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
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.Provisions;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.SchemaConfigurator;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.io.DataItem;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.DataStreamState;
import uk.co.strangeskies.modabi.processing.BindingBlock;
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
import uk.co.strangeskies.utilities.tuple.Pair;

public class BindingProviders {
	private interface ModelBindingProvider {
		<T> Set<T> getAndListen(Model<T> model, Function<? super T, Boolean> listener);
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
					public <T> Set<T> getAndListen(Model<T> model, Function<? super T, Boolean> listener) {
						/*
						 * 
						 * 
						 * TODO should really have an observable set directly over bindings,
						 * not be messing with bindingFutures here...
						 * 
						 * 
						 * 
						 */
						return manager.getBindingFutures(model).stream().filter(BindingFuture::isDone).map(BindingFuture::resolve)
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
						/*
						 * 
						 * 
						 * 
						 * 
						 * TODO context.bindings().get(model) should return a (synchronized)
						 * observable set rather than needing a separate changes method.
						 * 
						 * 
						 * 
						 * 
						 */
						synchronized (context.bindings()) {
							context.bindings().changes(model).addTerminatingObserver(listener);
							return context.bindings().get(model);
						}
					}
				}, idDomain, id, false);
			}
		};
	}

	private <U> U matchBinding(ProcessingContext context, Model<U> model, ModelBindingProvider bindings,
			QualifiedName idDomain, DataSource idSource, boolean externalDependency) {
		if (idSource.currentState() == DataStreamState.TERMINATED || idSource.isComplete())
			throw new BindingException("No further id data to match in domain '" + idDomain + "' for model '" + model + "'",
					context);

		/*
		 * Object property to contain result when we find a matching binding
		 */

		/*
		 * Create a validation function for the parameters of this dependency
		 */
		DataItem<?> id = idSource.get();

		ChildNode<?, ?> child = model.effective().child(idDomain);
		if (!(child instanceof DataNode.Effective<?>))
			throw new BindingException("Can't find child '" + idDomain + "' to target for model '" + model + "'", context);
		DataNode.Effective<?> idNode = (Effective<?>) child;

		/*
		 * Resolve dependency!
		 */
		Property<U, U> objectProperty = new IdentityProperty<>();
		Property<BindingBlock, BindingBlock> blockProperty = new IdentityProperty<>();

		Function<U, Boolean> validate = bindingCandidate -> {
			boolean success = validateBindingCandidate(bindingCandidate, model, idNode, id);
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
							blockProperty.get().complete();
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
			BindingBlock block = context.bindingFutureBlocker().block(model.getName(), id, !externalDependency);
			blockProperty.set(block);
		}

		return getProxiedBinding(model, blockProperty.get(), objectProperty::get);
	}

	private <U> boolean validateBindingCandidate(U objectCandidate, Model<U> model, DataNode.Effective<?> idNode,
			DataItem<?> id) {
		Objects.requireNonNull(objectCandidate);

		DataSource candidateId = unbindDataNode(idNode, new TypedObject<>(model.getDataType(), objectCandidate));

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
		Class<?> rawType = model.effective().getDataType().getRawType();

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
							blockAndObjectSupplier.getLeft().waitUntilComplete();
							object = blockAndObjectSupplier.getRight().get();

							blockAndObjectSupplier = null;
						}

						return method.invoke(object, args);
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
		provisions.registerProvider(SchemaConfigurator.class, schemaConfigurator());
		provisions.registerProvider(Imports.class, imports());
	}
}
