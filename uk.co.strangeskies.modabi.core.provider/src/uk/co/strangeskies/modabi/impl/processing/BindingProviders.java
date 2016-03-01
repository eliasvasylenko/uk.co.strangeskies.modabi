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
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.Provisions;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.io.BufferingDataTarget;
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

		/*
		 * Block thread
		 */
		Property<Thread, Thread> blockThread = new IdentityProperty<>();

		Property<U, U> objectProperty = new IdentityProperty<>();
		Function<U, Boolean> objectProvider = objectCandidate -> {
			synchronized (objectProperty) {
				if (objectProperty.get() == null) {
					Objects.requireNonNull(objectCandidate);

					DataSource candidateId = unbindDataNode(node, new TypedObject<>(model.getDataType(), objectCandidate));

					if (candidateId.size() == 1) {
						DataItem<?> candidateData = candidateId.get();

						if (id.data(candidateData.type()).equals(candidateData.data())) {
							objectProperty.set(objectCandidate);
							objectProperty.notifyAll();

							return true;
						}
					}
				}

				return false;
			}
		};

		Set<U> existingCandidates = bindings.getAndListen(model, objectCandidate -> {
			synchronized (objectProperty) {
				if (objectProvider.apply(objectCandidate) && blockThread.get() != null) {
					try {
						blockThread.get().join();
					} catch (InterruptedException e) {}
				}
			}
		});
		for (U objectCandidate : existingCandidates) {
			if (objectProvider.apply(objectCandidate)) {
				return objectCandidate;
			}
		}

		return getProxiedBinding(id, context, model, objectProperty, blockThread, externalDependency);
	}

	@SuppressWarnings("unchecked")
	private <U> U getProxiedBinding(DataItem<?> id, ProcessingContext context, Model<U> model,
			Property<U, U> objectProperty, Property<Thread, Thread> blockThread, boolean externalDependency) {
		/*
		 * Runnable to block until object is available
		 */
		Runnable waitForObject = () -> {
			try {
				synchronized (objectProperty) {
					while (objectProperty.get() == null) {
						objectProperty.wait();
					}
				}
			} catch (InterruptedException e) {}
		};

		/*
		 * Block binding until dependency is fulfilled
		 */
		DataSource bufferedId = new BufferingDataTarget().put(id).buffer();
		synchronized (objectProperty) {
			if (objectProperty.get() == null) {
				if (externalDependency) {
					blockThread.set(context.bindingFutureBlocker().blockFor(waitForObject, model.getName(), bufferedId));
				} else {
					blockThread.set(context.bindingFutureBlocker().blockForInternal(waitForObject, model.getName(), bufferedId));
				}
			}
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
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						context.bindingFutureBlocker().blockAndWaitForInternal(waitForObject, model.getName(), bufferedId);
						return method.invoke(objectProperty.get(), args);
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
