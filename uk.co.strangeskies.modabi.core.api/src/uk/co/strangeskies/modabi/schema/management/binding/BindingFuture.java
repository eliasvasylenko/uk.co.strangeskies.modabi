/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.modabi.schema.management.binding;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.Binding;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.Model;

public interface BindingFuture<T> extends Future<Binding<T>> {
	QualifiedName getName();

	Model<T> getModel();

	Set<BindingFuture<?>> getBlockingBindings();

	default Binding<T> resolve() {
		try {
			return get();
		} catch (InterruptedException e) {
			throw new SchemaException("Unexpected interrupt during binding of '"
					+ getName() + "' with model '" + getModel().getName() + "'.", e);
		} catch (ExecutionException e) {
			throw new SchemaException("Exception during binding of '" + getName()
					+ "' with model '" + getModel().getName() + "'.", e.getCause());
		}
	}

	default Binding<T> resolveNow() {
		Set<BindingFuture<?>> blockingBindings = getBlockingBindings();

		if (!isDone() && cancel(true))
			throw new SchemaException(
					"Binding has been blocked by the following missing dependencies: "
							+ blockingBindings);

		return resolve();
	}

	static <U> BindingFuture<U> forData(Model<U> model, U data) {
		Binding<U> binding = new Binding<U>(model, data);

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
			public Binding<U> get() throws InterruptedException, ExecutionException {
				return binding;
			}

			@Override
			public Binding<U> get(long timeout, TimeUnit unit) {
				return binding;
			}

			@Override
			public QualifiedName getName() {
				return model.getName();
			}

			@Override
			public Model<U> getModel() {
				return model;
			}

			@Override
			public Set<BindingFuture<?>> getBlockingBindings() {
				return new HashSet<>();
			}
		};
	}
}
