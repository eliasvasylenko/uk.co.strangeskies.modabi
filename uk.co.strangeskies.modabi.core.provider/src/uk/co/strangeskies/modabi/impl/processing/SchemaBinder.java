/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.processing.BindingException;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.schema.Model;

public class SchemaBinder {
	private static interface TryGet<T> {
		T tryGet()
				throws InterruptedException, ExecutionException, TimeoutException;
	}

	private final BindingContextImpl context;

	public SchemaBinder(BindingContextImpl context) {
		this.context = context;
	}

	public <T> BindingFuture<T> bind(Model.Effective<T> model,
			StructuredDataSource input) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		BindingContextImpl context = this.context.withInput(input);

		QualifiedName inputRoot = input.startNextChild();
		if (!inputRoot.equals(model.getName()))
			throw new BindingException("Model '" + model.getName()
					+ "' does not match root input node '" + inputRoot + "'", context);

		FutureTask<T> future = new FutureTask<>(() -> {
			Thread.currentThread().setContextClassLoader(classLoader);

			try {
				return new BindingNodeBinder(context).bind(model);
			} catch (SchemaException e) {
				throw e;
			} catch (Exception e) {
				throw new BindingException("Unexpected problem during binding.",
						context, e);
			}
		});
		future.run();

		return new BindingFuture<T>() {
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isCancelled() {
				return future.isCancelled();
			}

			@Override
			public boolean isDone() {
				return future.isDone();
			}

			@Override
			public Binding<T> get() {
				return tryGet(future::get);
			}

			@Override
			public Binding<T> get(long timeout, TimeUnit unit) {
				return tryGet(() -> future.get(timeout, unit));
			}

			private Binding<T> tryGet(TryGet<T> get) {
				try {
					return new Binding<T>(getModel(), get.tryGet());
				} catch (InterruptedException e) {
					throw new SchemaException("Unexpected interrupt during binding of '"
							+ getName() + "' with model '" + getModel().getName() + "'", e);
				} catch (ExecutionException e) {
					throw new SchemaException("Exception during binding of '" + getName()
							+ "' with model '" + getModel().getName() + "'", e.getCause());
				} catch (TimeoutException e) {
					throw new SchemaException(
							"Timed out waiting for binding of '" + getName()
									+ "' with model '" + getModel().getName() + "'",
							e.getCause());
				}
			}

			@Override
			public QualifiedName getName() {
				return model.getName();
			}

			@Override
			public Model<T> getModel() {
				return model;
			}

			@Override
			public Set<BindingFuture<?>> getBlockingBindings() {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}
}
