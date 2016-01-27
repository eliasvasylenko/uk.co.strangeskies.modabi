/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.api.
 *
 * uk.co.strangeskies.modabi.core.api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.api.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.processing;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.schema.Model;

public interface BindingFuture<T> extends Future<Binding<T>> {
	Model<T> getModel();

	Set<BindingFuture<?>> getBlockingBindings();

	@Override
	Binding<T> get();

	@Override
	Binding<T> get(long timeout, TimeUnit unit);

	default T resolve() {
		return get().getData();
	}

	default T resolve(long timeout) {
		return get(timeout, TimeUnit.MILLISECONDS).getData();
	}

	default Binding<T> getNow() {
		Set<BindingFuture<?>> blockingBindings = getBlockingBindings();

		if (!isDone() && cancel(true))
			throw new SchemaException("Binding has been blocked by the following missing dependencies: " + blockingBindings);

		return get();
	}

	default T resolveNow() {
		return getNow().getData();
	}

	static <U> BindingFuture<U> forBinding(Binding<U> binding) {
		return new BindingFuture<U>() {
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return false;
			}

			@Override
			public boolean isCancelled() {
				return false;
			}

			@Override
			public boolean isDone() {
				return true;
			}

			@Override
			public Binding<U> get() {
				return binding;
			}

			@Override
			public Binding<U> get(long timeout, TimeUnit unit) {
				return binding;
			}

			@Override
			public Model<U> getModel() {
				return binding.getModel();
			}

			@Override
			public Set<BindingFuture<?>> getBlockingBindings() {
				return new HashSet<>();
			}
		};
	}

	static <T> BindingFuture<T> forFuture(Model<T> model, Future<T> future) {
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
				String input = "TEMP"; // TODO

				try {
					return new Binding<T>() {
						private T data = get.tryGet();

						@Override
						public Model<T> getModel() {
							return model;
						}

						@Override
						public T getData() {
							return data;
						}
					};
				} catch (InterruptedException e) {
					throw new SchemaException(
							"Unexpected interrupt during binding of '" + input + "' with model '" + getModel().getName() + "'", e);
				} catch (ExecutionException e) {
					throw new SchemaException(
							"Exception during binding of '" + input + "' with model '" + getModel().getName() + "'", e.getCause());
				} catch (TimeoutException e) {
					throw new SchemaException(
							"Timed out waiting for binding of '" + input + "' with model '" + getModel().getName() + "'",
							e.getCause());
				}
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

	interface TryGet<T> {
		T tryGet() throws InterruptedException, ExecutionException, TimeoutException;
	}
}
