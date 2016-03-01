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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.Provisions;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.io.DataItem;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.DataStreamState;
import uk.co.strangeskies.modabi.processing.BindingException;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.providers.DereferenceSource;
import uk.co.strangeskies.modabi.processing.providers.ImportSource;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataNode.Effective;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypedObject;
import uk.co.strangeskies.utilities.ConsumerSupplierQueue;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Property;

public class BindingProviders {
	private interface ModelBindingProvider {
		<T> Set<T> getAndListen(Model<T> model, Consumer<? super T> listener);
	}

	private final SchemaManager manager;

	public BindingProviders(SchemaManager manager) {
		this.manager = manager;
	}

	public Function<ProcessingContext, ImportSource> importSource() {
		return context -> new ImportSource() {
			@Override
			public <U> U importObject(Model<U> model, QualifiedName idDomain, DataSource id) {
				return matchBinding(context, model, new ModelBindingProvider() {
					@Override
					public <T> Set<T> getAndListen(Model<T> model, Consumer<? super T> listener) {
						return manager.bindingFutures(model).stream().filter(BindingFuture::isDone).map(BindingFuture::resolve)
								.collect(Collectors.toSet());
					}
				}, idDomain, id, true);
			}
		};
	}

	public Function<ProcessingContext, Imports> imports() {
		return context -> Imports.empty();
	}

	/*
	 * TODO from BindingContext not ProcessingContextImpl
	 */
	public Function<ProcessingContext, DataLoader> dataLoader() {
		return context -> new DataLoader() {
			@Override
			public <U> List<U> loadData(DataNode<U> node, DataSource data) {
				return new DataNodeBinder<>(context, node.effective()).getBinding();
			}
		};
	}

	public Function<ProcessingContext, DereferenceSource> dereferenceSource() {
		return context -> new DereferenceSource() {
			@Override
			public <U> U dereference(Model<U> model, QualifiedName idDomain, DataSource id, boolean externalDependency) {
				return matchBinding(context, model, context.bindings()::addListener, idDomain, id, externalDependency);
			}
		};
	}

	@SuppressWarnings("unchecked")
	private <U> U matchBinding(ProcessingContext context, Model<U> model, ModelBindingProvider bindings,
			QualifiedName idDomain, DataSource idSource, boolean externalDependency) {
		if (idSource.currentState() == DataStreamState.TERMINATED)
			throw new BindingException("No further id data to match in domain '" + idDomain + "' for model '" + model + "'",
					context);

		DataItem<?> id = idSource.get();

		ChildNode<?, ?> child = model.effective().child(idDomain);
		if (!(child instanceof DataNode.Effective<?>))
			throw new BindingException("Can't find child '" + idDomain + "' to target for model '" + model + "'", context);
		DataNode.Effective<?> node = (Effective<?>) child;

		Property<U, U> objectProperty = new IdentityProperty<>();
		Consumer<Iterable<U>> objectProvider = bindingCandidates -> {
			for (U bindingCandidate : bindingCandidates) {
				DataSource candidateId = unbindDataNode(node, new TypedObject<>(model.getDataType(), bindingCandidate));

				if (candidateId.size() != 1)
					continue;

				DataItem<?> candidateData = candidateId.get();

				if (id.data(candidateData.type()).equals(candidateData.data())) {
					objectProperty.set(bindingCandidate);
				}
			}
		};

		ConsumerSupplierQueue<U> queue = new ConsumerSupplierQueue<>();
		objectProvider.accept(bindings.getAndListen(model, queue));

		Thread fetchThread;
		if (objectProperty.get() == null) {
			if (externalDependency) {
				fetchThread = context.bindingFutureBlocker().blockFor(() -> objectProvider.accept(queue), model.getName(),
						idSource);
			} else {
				throw new BindingException(
						"Can't find any bindings matching id '" + id + "' in domain '" + idDomain + "' for model '" + model + "'",
						context);
			}
		} else {
			fetchThread = null;
		}

		/*
		 * Should only have one raw type. Non-abstract models shouldn't be
		 * intersection types.
		 */
		Class<?> rawType = model.effective().getDataType().getRawType();

		/*
		 * TODO check if raw type is actually proxiable...
		 */

		return (U) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] { rawType },
				new InvocationHandler() {
					private Property<U, U> object = objectProperty;
					private Thread thread = fetchThread;

					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						if (thread != null) {
							thread.join();
							thread = null;
						}
						return method.invoke(object.get(), args);
					}
				});
	}

	private <V> DataSource unbindDataNode(DataNode.Effective<V> node, TypedObject<?> source) {
		ProcessingContextImpl unbindingContext = new ProcessingContextImpl(manager).withBindingObject(source);

		return new DataNodeUnbinder(unbindingContext).unbindToDataBuffer(node,
				BindingNodeUnbinder.getData(node, unbindingContext));
	}

	public void registerProviders(Provisions provisions) {
		provisions.registerProvider(DereferenceSource.class, dereferenceSource());
		provisions.registerProvider(ImportSource.class, importSource());
		provisions.registerProvider(DataLoader.class, dataLoader());
		provisions.registerProvider(Imports.class, imports());
	}
}
