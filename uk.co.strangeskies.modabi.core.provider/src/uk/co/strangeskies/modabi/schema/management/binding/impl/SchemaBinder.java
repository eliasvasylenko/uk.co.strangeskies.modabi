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
package uk.co.strangeskies.modabi.schema.management.binding.impl;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.Binding;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.management.SchemaManager;
import uk.co.strangeskies.modabi.schema.management.binding.BindingContext;
import uk.co.strangeskies.modabi.schema.management.binding.BindingException;
import uk.co.strangeskies.modabi.schema.management.binding.BindingFuture;
import uk.co.strangeskies.modabi.schema.management.providers.DereferenceSource;
import uk.co.strangeskies.modabi.schema.management.providers.ImportSource;
import uk.co.strangeskies.modabi.schema.management.providers.IncludeTarget;
import uk.co.strangeskies.modabi.schema.management.providers.TypeParser;
import uk.co.strangeskies.modabi.schema.management.providers.impl.BindingProviders;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.modabi.schema.node.model.Model;

public class SchemaBinder {
	private final BindingContextImpl context;

	public SchemaBinder(SchemaManager manager) {
		BindingProviders providers = new BindingProviders(manager);

		context = new BindingContextImpl(manager)
				.withProvision(DereferenceSource.class, providers.dereferenceSource())
				.withProvision(IncludeTarget.class, providers.includeTarget())
				.withProvision(ImportSource.class, providers.importSource())
				.withProvision(DataLoader.class, providers.dataLoader())
				.withProvision(TypeParser.class, providers.typeParser())
				.withProvision(BindingContext.class, c -> c);
	}

	public <T> BindingFuture<T> bind(Model.Effective<T> model,
			StructuredDataSource input) {
		BindingContextImpl context = this.context.withInput(input);

		QualifiedName inputRoot = input.startNextChild();
		if (!inputRoot.equals(model.getName()))
			throw new BindingException("Model '" + model.getName()
					+ "' does not match root input node '" + inputRoot + "'.", context);

		FutureTask<T> future = new FutureTask<>(() -> {
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
			public Binding<T> get() throws InterruptedException, ExecutionException {
				return new Binding<T>(getModel(), future.get());
			}

			@Override
			public Binding<T> get(long timeout, TimeUnit unit)
					throws InterruptedException, ExecutionException, TimeoutException {
				return new Binding<T>(getModel(), future.get(timeout, unit));
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
