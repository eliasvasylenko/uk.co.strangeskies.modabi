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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.utilities.tuple.Pair;

public interface BindingFuture<T> extends Future<Binding<T>> {
	Future<Model<T>> getModelFuture();

	BindingFutureBlocks getBlocks();

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
		BindingFutureBlocks blockingBindings = getBlocks();

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
			public Future<Model<U>> getModelFuture() {
				FutureTask<Model<U>> modelFuture = new FutureTask<>(binding::getModel);
				modelFuture.run();
				return modelFuture;
			}

			@Override
			public BindingFutureBlocks getBlocks() {
				return new BindingFutureBlocks() {
					@Override
					public boolean removeObserver(Consumer<? super Pair<QualifiedName, DataSource>> observer) {
						return true;
					}

					@Override
					public boolean addObserver(Consumer<? super Pair<QualifiedName, DataSource>> observer) {
						return true;
					}

					@Override
					public Set<QualifiedName> waitingForNamespaces() {
						return Collections.emptySet();
					}

					@Override
					public List<DataSource> waitingForIds(QualifiedName namespace) {
						return Collections.emptyList();
					}

					@Override
					public void waitForAll(QualifiedName namespace, long timeoutMilliseconds) {}

					@Override
					public void waitForAll(QualifiedName namespace) {}

					@Override
					public void waitFor(QualifiedName namespace, DataSource id, long timeoutMilliseconds) {}

					@Override
					public void waitFor(QualifiedName namespace, DataSource id) {}

					@Override
					public boolean isBlocked() {
						return false;
					}
				};
			}
		};
	}
}
