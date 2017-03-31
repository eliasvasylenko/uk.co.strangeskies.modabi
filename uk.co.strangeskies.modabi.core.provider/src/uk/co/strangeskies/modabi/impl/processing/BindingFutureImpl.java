/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.provider.
 *
 * uk.co.strangeskies.modabi.core.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.impl.processing;

import java.io.InputStream;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;
import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.processing.BindingBlock;
import uk.co.strangeskies.modabi.processing.BindingBlocks;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Property;
import uk.co.strangeskies.utilities.function.ThrowingSupplier;

public class BindingFutureImpl<T> implements BindingFuture<T> {
	private interface TryGet<T> {
		T tryGet() throws InterruptedException, ExecutionException, TimeoutException;
	}

	public static class BindingSource<T> {
		private final Model<T> model;
		private final Consumer<Consumer<StructuredDataSource>> data;

		public BindingSource(Model<T> model, StructuredDataSource data) {
			this.model = model;
			this.data = c -> c.accept(data);
		}

		public BindingSource(Model<T> model, ThrowingSupplier<InputStream, ?> input, StructuredDataFormat format) {
			this.model = model;
			this.data = c -> {
				try (InputStream stream = input.get()) {
					c.accept(format.loadData(stream));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			};
		}

		public Model<T> getModel() {
			return model;
		}

		public void withData(Consumer<StructuredDataSource> runnable) {
			data.accept(runnable);
		}

		public <U> U withData(Function<StructuredDataSource, U> runnable) {
			Property<U, U> result = new IdentityProperty<>();
			data.accept(input -> {
				result.set(runnable.apply(input));
			});
			return result.get();
		}

		public String getName() {
			return null;
		}
	}

	private final ProcessingContextImpl context;

	private final FutureTask<BindingSource<T>> sourceFuture;
	private final FutureTask<T> dataFuture;
	private final FutureTask<Model<T>> modelFuture;

	private Binding<T> bindingResult;
	private boolean cancelled;

	public BindingFutureImpl(ProcessingContextImpl context, ClassLoader classLoader,
			Supplier<BindingSource<T>> modelSupplier) {
		this.context = context;

		cancelled = false;

		sourceFuture = new FutureTask<>(modelSupplier::get);
		modelFuture = new FutureTask<>(() -> {
			sourceFuture.run();
			return sourceFuture.get().getModel();
		});

		dataFuture = new FutureTask<>(() -> {
			Thread.currentThread().setContextClassLoader(classLoader);
			context.bindingBlocker().addParticipatingThread(Thread.currentThread());

			modelFuture.run();

			T binding = bind(sourceFuture.get());

			context.bindingBlocker().complete();

			return binding;
		});
		new Thread(() -> dataFuture.run()).start();
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		synchronized (context.bindingBlocker()) {
			if (bindingResult == null && !cancelled) {
				Exception exception = new ModabiException(t -> t.cancelled(this));
				for (BindingBlock block : context.bindingBlocker().getBlocks()) {
					block.fail(exception);
				}
				for (Thread thread : context.bindingBlocker().getParticipatingThreads()) {
					thread.interrupt();
				}
				cancelled = true;
				return sourceFuture.cancel(mayInterruptIfRunning) | dataFuture.cancel(mayInterruptIfRunning)
						| modelFuture.cancel(mayInterruptIfRunning);
			} else {
				return false;
			}
		}
	}

	@Override
	public boolean isCancelled() {
		return dataFuture.isCancelled();
	}

	@Override
	public boolean isDone() {
		return dataFuture.isDone();
	}

	@Override
	public Binding<T> get() {
		return tryGet(() -> sourceFuture.get(), dataFuture::get);
	}

	@Override
	public Binding<T> get(long timeout, TimeUnit unit) {
		return tryGet(() -> sourceFuture.get(timeout, unit), () -> dataFuture.get(timeout, unit));
	}

	private Binding<T> tryGet(TryGet<BindingSource<T>> getModel, TryGet<T> getData) {
		synchronized (context.bindingBlocker()) {
			if (bindingResult != null) {
				return bindingResult;
			}

			if (cancelled) {
				throw new CancellationException();
			}
		}

		String input = "unknown";

		String modelString = "";
		try {
			BindingSource<T> source = getModel.tryGet();
			Model<T> model = source.getModel();

			modelString = " with model '" + model.name() + "'";

			input = source.getName();

			T data = getData.tryGet();

			bindingResult = new Binding<>(model, data);

			return bindingResult;
		} catch (InterruptedException e) {
			throw new ProcessingException(
					"Unexpected interrupt during binding of '" + input + "' with blocks '" + blocks() + "'" + modelString,
					context, e);
		} catch (ExecutionException e) {
			throw new ProcessingException(
					"Exception during binding of '" + input + "' with blocks '" + blocks() + "'" + modelString, context,
					e.getCause());
		} catch (TimeoutException e) {
			throw new ProcessingException(
					"Timed out waiting for binding of '" + input + "' with blocks '" + blocks() + "'" + modelString, context, e);
		}
	}

	@Override
	public Future<Model<T>> getModelFuture() {
		return modelFuture;
	}

	@Override
	public BindingBlocks blocks() {
		return context.bindingBlocker();
	}

	private T bind(BindingSource<T> source) {
		Model<T> model = source.getModel();
		return source.withData((StructuredDataSource input) -> {
			ProcessingContextImpl context = this.context.withInput(input);

			/*
			 * Processing should always have access to the contents of the schema
			 * owning the associated model.
			 */
			for (Model<?> schemaModel : model.schema().models()) {
				context.bindings().add(context.manager().getMetaSchema().getMetaModel(), schemaModel);
			}

			QualifiedName inputRoot = input.startNextChild();
			if (!inputRoot.equals(model.name()))
				throw new ProcessingException("Model '" + model.name() + "' does not match root input node '" + inputRoot + "'",
						context);

			try {
				return new BindingNodeBinder(context).bind(model);
			} catch (ModabiException e) {
				throw e;
			} catch (Exception e) {
				throw new ProcessingException(t -> t.unexpectedProblemProcessing(source, model), context, e);
			}
		});
	}
}
