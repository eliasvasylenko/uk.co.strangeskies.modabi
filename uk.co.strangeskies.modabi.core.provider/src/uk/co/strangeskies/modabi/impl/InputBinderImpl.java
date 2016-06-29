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
package uk.co.strangeskies.modabi.impl;

import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import uk.co.strangeskies.modabi.DataFormats;
import uk.co.strangeskies.modabi.InputBinder;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.impl.processing.BindingFutureImpl;
import uk.co.strangeskies.modabi.impl.processing.BindingFutureImpl.BindingSource;
import uk.co.strangeskies.modabi.impl.processing.ProcessingContextImpl;
import uk.co.strangeskies.modabi.io.Primitive;
import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;
import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.processing.BindingBlock;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.ConsumerSupplierQueue;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Property;
import uk.co.strangeskies.utilities.classpath.ManifestUtilities;
import uk.co.strangeskies.utilities.collection.ObservableSet.Change;
import uk.co.strangeskies.utilities.function.ThrowingSupplier;

public class InputBinderImpl<T> implements InputBinder<T> {
	private static final QualifiedName FORMAT_BLOCK_NAMESPACE = new QualifiedName("structuredDataFormat",
			Schema.MODABI_NAMESPACE);

	private final ProcessingContextImpl context;
	private final DataFormats formats;
	private final Consumer<BindingFuture<?>> complete;

	private final Function<StructuredDataSource, Model<T>> bindingModelFunction;

	private ClassLoader classLoader;

	protected InputBinderImpl(ProcessingContextImpl context, DataFormats formats, Consumer<BindingFuture<?>> complete,
			Function<StructuredDataSource, Model<T>> bindingModelFunction) {
		this.context = context;
		this.formats = formats;
		this.complete = complete;

		this.bindingModelFunction = bindingModelFunction;
	}

	public static InputBinder<?> bind(ProcessingContextImpl context, DataFormats formats,
			Consumer<BindingFuture<?>> complete) {
		return new InputBinderImpl<>(context, formats, complete, data -> {
			try {
				return context.registeredModels().waitForGet(data.peekNextChild());
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
	}

	protected <U> InputBinder<U> with(Function<StructuredDataSource, Model<U>> bindingModelFunction) {
		return new InputBinderImpl<>(context, formats, complete, bindingModelFunction);
	}

	@Override
	public <U> InputBinder<U> with(Model<U> model) {
		return with(data -> {
			if (!context.registeredModels().contains(model)) {
				throw new ProcessingException("", context);
			}

			if (!data.peekNextChild().equals(model.name())) {
				throw new ProcessingException("", context);
			}

			return model;
		});
	}

	@Override
	public <U> InputBinder<U> with(TypeToken<U> type) {
		return with(data -> {
			try {
				return context.registeredModels().waitForGet(data.peekNextChild(), type);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public InputBinder<?> with(QualifiedName name) {
		return with(data -> {
			try {
				return context.registeredModels().waitForGet(name);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public <U> InputBinder<U> with(QualifiedName name, TypeToken<U> type) {
		return with(data -> {
			try {
				return context.registeredModels().waitForGet(name, type);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public BindingFuture<T> from(URL input) {
		String extension = ManifestUtilities.getResourceExtension(input.getPath());

		if (extension != null) {
			return fromExtension(extension, input::openStream);
		} else {
			return from(input::openStream);
		}
	}

	@Override
	public BindingFuture<T> from(StructuredDataSource input) {
		return createBindingFuture(() -> new BindingSource<>(bindingModelFunction.apply(input).effective(), input));
	}

	@Override
	public BindingFuture<T> from(ThrowingSupplier<InputStream, ?> input) {
		return createBindingFuture(() -> getBindingSource(null, input, f -> true, true));
	}

	@Override
	public BindingFuture<T> from(String formatId, ThrowingSupplier<InputStream, ?> input) {
		return createBindingFuture(() -> getBindingSource(formatId, input, f -> f.getFormatId().equals(formatId), false));
	}

	private BindingFuture<T> fromExtension(String extension, ThrowingSupplier<InputStream, ?> input) {
		return createBindingFuture(
				() -> getBindingSource(extension, input, f -> f.getFileExtensions().contains(extension), true));
	}

	private BindingFuture<T> createBindingFuture(Supplier<BindingSource<T>> modelSupplier) {
		ClassLoader classLoader = this.classLoader != null ? this.classLoader
				: Thread.currentThread().getContextClassLoader();
		BindingFuture<T> bindingFuture = new BindingFutureImpl<>(context, classLoader, modelSupplier);

		complete.accept(bindingFuture);

		return bindingFuture;
	}

	private BindingSource<T> getBindingSource(String formatId, ThrowingSupplier<InputStream, ?> input,
			Predicate<StructuredDataFormat> formatPredicate, boolean canRetry) {
		Property<Exception, Exception> exception = new IdentityProperty<>();

		Function<Iterable<StructuredDataFormat>, BindingSource<T>> getBindingSource = formats -> {
			for (StructuredDataFormat format : formats) {
				if (formatPredicate.test(format)) {
					try (InputStream inputStream = input.get()) {
						StructuredDataSource source = format.loadData(inputStream);
						Model.Effective<T> model = bindingModelFunction.apply(source).effective();

						return new BindingSource<>(model, input, format);
					} catch (Exception e) {
						exception.set(e);

						if (!canRetry) {
							throw new ProcessingException(t -> formatId == null ? t.noFormatFound() : t.noFormatFoundFor(formatId),
									context, exception.get());
						}
					}
				}
			}

			return null;
		};

		ConsumerSupplierQueue<StructuredDataFormat> queue = new ConsumerSupplierQueue<>();
		Set<StructuredDataFormat> registeredFormats = new HashSet<>();

		Consumer<Change<StructuredDataFormat>> observer = change -> {
			synchronized (registeredFormats) {
				for (StructuredDataFormat format : change.added()) {
					if (!registeredFormats.contains(format)) {
						queue.accept(format);
					}
				}
			}
		};

		synchronized (registeredFormats) {
			formats.changes().addWeakObserver(observer);
			registeredFormats.addAll(formats);
		}

		try {
			BindingSource<T> source = getBindingSource.apply(registeredFormats);

			if (source == null) {
				BindingBlock block = context.bindingFutureBlocker().block(FORMAT_BLOCK_NAMESPACE, Primitive.STRING, formatId,
						false);

				try {
					source = getBindingSource.apply(queue);
					block.complete();
				} catch (Exception e) {
					block.fail(e);
				}
			}

			return source;
		} catch (Exception e) {
			if (exception.get() == null)
				exception.set(e);

			throw new ProcessingException(t -> t.noFormatFoundFor(formatId), context, exception.get());
		}
	}

	@Override
	public InputBinder<T> withProvider(Provider provider) {
		context.provisions().add(provider);
		return this;
	}

	@Override
	public InputBinder<T> withErrorHandler(Consumer<Exception> errorHandler) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputBinder<T> withClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
		return this;
	}
}
