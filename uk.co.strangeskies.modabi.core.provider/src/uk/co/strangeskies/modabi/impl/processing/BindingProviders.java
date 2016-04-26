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

import static java.util.stream.Collectors.toList;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.ChildNodeBinding;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.Provisions;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.SchemaConfigurator;
import uk.co.strangeskies.modabi.io.DataItem;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.DataStreamState;
import uk.co.strangeskies.modabi.processing.BindingBlock;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.processing.providers.DereferenceSource;
import uk.co.strangeskies.modabi.processing.providers.ImportSource;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataNode.Effective;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypedObject;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Property;
import uk.co.strangeskies.utilities.collection.ObservableSet;
import uk.co.strangeskies.utilities.tuple.Pair;

public class BindingProviders {
	private interface ModelBindingProvider {
		<T> Set<T> getAndListen(Model<T> model, Function<? super T, Boolean> listener);
	}

	public Function<ProcessingContext, ImportSource> importSource() {
		return context -> new ImportSource() {
			@Override
			public <U> U importObject(Model<U> model, QualifiedName idDomain, DataSource id) {
				return matchBinding(context, model, new ModelBindingProvider() {
					@Override
					public <T> Set<T> getAndListen(Model<T> model, Function<? super T, Boolean> listener) {
						ObservableSet<?, Binding<T>> bindings = context.manager().getBindings(model);

						synchronized (bindings) {
							bindings.changes().addTerminatingObserver(b -> {
								for (Binding<T> binding : b.added()) {
									if (!listener.apply(binding.getData())) {
										return false;
									}
								}
								return true;
							});
							Set<T> s = bindings.stream().map(Binding::getData).collect(Collectors.toSet());
							return s;
						}
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
				return new DataNodeBinder<>(context, node.effective()).getBinding().stream().map(ChildNodeBinding::getData)
						.collect(toList());
			}
		};
	}

	public Function<ProcessingContext, SchemaConfigurator> schemaConfigurator() {
		return context -> context.provide(SchemaBuilder.class).getObject()
				.configure(context.provide(DataLoader.class).getObject());
	}

	public Function<ProcessingContext, DereferenceSource> dereferenceSource() {
		return context -> new DereferenceSource() {
			@Override
			public <U> U dereference(Model<U> model, QualifiedName idDomain, DataSource id) {
				return matchBinding(context, model, new ModelBindingProvider() {
					@Override
					public <T> Set<T> getAndListen(Model<T> model, Function<? super T, Boolean> listener) {
						synchronized (context.bindings()) {
							context.bindings().changes(model).addTerminatingObserver(listener);
							return context.bindings().getModelBindings(model);
						}
					}
				}, idDomain, id, false);
			}

			public <T> T dereference(BindingNode<T, ?, ?> node) {
				// TODO
				return null;
			}
		};
	}

	private <U> U matchBinding(ProcessingContext context, Model<U> model, ModelBindingProvider bindings,
			QualifiedName idDomain, DataSource idSource, boolean externalDependency) {
		if (idSource.currentState() == DataStreamState.TERMINATED || idSource.isComplete())
			throw new ProcessingException(
					"No further id data to match in domain '" + idDomain + "' for model '" + model + "'", context);

		/*
		 * Object property to contain result when we find a matching binding
		 */

		/*
		 * Create a validation function for the parameters of this dependency
		 */
		DataItem<?> id = idSource.get();

		ChildNode<?, ?> child = model.effective().child(idDomain);
		if (!(child instanceof DataNode.Effective<?>))
			throw new ProcessingException("Can't find child '" + idDomain + "' to target for model '" + model + "'", context);
		DataNode.Effective<?> idNode = (Effective<?>) child;

		/*
		 * Resolve dependency!
		 */
		Property<U, U> objectProperty = new IdentityProperty<>();
		Property<BindingBlock, BindingBlock> blockProperty = new IdentityProperty<>();

		Function<U, Boolean> validate = bindingCandidate -> {
			boolean success = validateBindingCandidate(context, bindingCandidate, model, idNode, id);
			if (success) {
				objectProperty.set(bindingCandidate);
			}
			return success;
		};

		synchronized (objectProperty) {
			Set<U> existingCandidates = bindings.getAndListen(model, objectCandidate -> {
				synchronized (objectProperty) {
					if (objectProperty.get() == null) {
						if (validate.apply(objectCandidate)) {
							objectProperty.set(objectCandidate);
							try {
								blockProperty.get().complete();
							} catch (ExecutionException e) {
								throw new RuntimeException(e.getCause());
							}
							return false;
						} else {
							return true;
						}
					} else {
						return false;
					}
				}
			});

			/*
			 * Check existing candidates to fulfil dependency
			 */
			for (U objectCandidate : existingCandidates) {
				if (validate.apply(objectCandidate)) {
					return objectCandidate;
				}
			}

			/*
			 * No existing candidates found, so block to wait for new ones
			 */
			BindingBlock block = context.bindingFutureBlocker().block(model.name(), id, !externalDependency);
			blockProperty.set(block);
		}

		return getProxiedBinding(model, blockProperty.get(), objectProperty::get);
	}

	private <U> boolean validateBindingCandidate(ProcessingContext context, U bindingCandidate, Model<U> model,
			DataNode.Effective<?> idNode, DataItem<?> id) {
		Objects.requireNonNull(bindingCandidate);

		DataSource candidateId = unbindDataNode(context, idNode, new TypedObject<>(model.dataType(), bindingCandidate));

		if (candidateId.size() == 1) {
			DataItem<?> candidateData = candidateId.get();

			if (id.data(candidateData.type()).equals(candidateData.data())) {
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	private <U> U getProxiedBinding(Model<U> model, BindingBlock block, Supplier<U> objectSupplier) {
		/*
		 * Should only have one raw type. Non-abstract models shouldn't be
		 * intersection types.
		 */
		Class<?> rawType = model.effective().dataType().getRawType();

		/*
		 * TODO check if raw type is actually proxiable...
		 */
		return (U) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] { rawType },
				new InvocationHandler() {
					/*
					 * Use a Pair so we can reduce the extra memory footprint to a single
					 * nullable reference...
					 */
					private Pair<BindingBlock, Supplier<U>> blockAndObjectSupplier = new Pair<>(block, objectSupplier);
					private U object;

					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						if (object == null) {
							try {
								blockAndObjectSupplier.getLeft().waitUntilComplete();
							} catch (Exception e) {
								throw new RuntimeException("Problem waiting for " + blockAndObjectSupplier.getLeft(), e);
							}
							object = blockAndObjectSupplier.getRight().get();

							blockAndObjectSupplier = null;
						}

						return method.invoke(object, args);
					}
				});
	}

	private <V> DataSource unbindDataNode(ProcessingContext context, DataNode.Effective<V> node, TypedObject<?> source) {
		ProcessingContextImpl unbindingContext = new ProcessingContextImpl(context).withBindingObject(source);

		return new DataNodeUnbinder(unbindingContext).unbindToDataBuffer(node,
				BindingNodeUnbinder.getData(node, unbindingContext));
	}

	public void registerProviders(Provisions provisions) {
		provisions.add(Provider.over(DereferenceSource.class, dereferenceSource()));
		provisions.add(Provider.over(ImportSource.class, importSource()));
		provisions.add(Provider.over(DataLoader.class, dataLoader()));
		provisions.add(Provider.over(SchemaConfigurator.class, schemaConfigurator()));
		provisions.add(Provider.over(Imports.class, imports()));
	}
}
