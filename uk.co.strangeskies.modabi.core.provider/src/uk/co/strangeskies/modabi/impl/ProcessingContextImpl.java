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
package uk.co.strangeskies.modabi.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.Bindings;
import uk.co.strangeskies.modabi.Provisions;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.DataBindingType;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.building.DataBindingTypeBuilder;
import uk.co.strangeskies.modabi.schema.building.ModelBuilder;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.Self;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;
import uk.co.strangeskies.utilities.collection.computingmap.DeferredComputingMap;
import uk.co.strangeskies.utilities.collection.computingmap.LRUCacheComputingMap;
import uk.co.strangeskies.utilities.factory.Factory;

public abstract class ProcessingContextImpl<S extends ProcessingContextImpl<S>>
		implements Self<S> {
	protected abstract class ProcessingProvisions {
		public abstract <U> U provide(TypeToken<U> clazz, S headContext);

		public abstract boolean isProvided(TypeToken<?> clazz);
	}

	public enum CacheScope {
		MANAGER_GLOBAL, PROCESSING_CONTEXT
	}

	private final SchemaManager manager;

	private final List<SchemaNode.Effective<?, ?>> nodeStack;
	private final Bindings bindings; // TODO erase bindings in failed sections

	private final ComputingMap<DataNode<?>, ComputingMap<? extends DataBindingType<?>, ? extends DataNode.Effective<?>>> dataTypeCache;
	private final ComputingMap<ComplexNode<?>, ComputingMap<? extends Model<?>, ? extends ComplexNode.Effective<?>>> modelCache;

	private final ProcessingProvisions provider;

	public ProcessingContextImpl(SchemaManager manager) {
		this.manager = manager;

		nodeStack = Collections.emptyList();
		bindings = new Bindings();

		dataTypeCache = new LRUCacheComputingMap<>(
				node -> getDataNodeOverrideMap(node.effective()), 150, true);

		modelCache = new LRUCacheComputingMap<>(
				node -> getComplexNodeOverrideMap(node.effective()), 150, true);

		provider = new ProcessingProvisions() {
			@Override
			public <U> U provide(TypeToken<U> clazz, S headContext) {
				return manager.provisions().provide(clazz);
			}

			@Override
			public boolean isProvided(TypeToken<?> clazz) {
				return manager.provisions().isProvided(clazz);
			}
		};
	}

	public ProcessingContextImpl(ProcessingContextImpl<S> parentContext,
			ProcessingProvisions provider) {
		manager = parentContext.manager;

		nodeStack = parentContext.nodeStack;
		bindings = parentContext.bindings;

		dataTypeCache = parentContext.dataTypeCache;
		modelCache = parentContext.modelCache;

		this.provider = provider;
	}

	public ProcessingContextImpl(ProcessingContextImpl<S> parentContext,
			SchemaNode.Effective<?, ?> bindingNode) {
		manager = parentContext.manager;

		List<SchemaNode.Effective<?, ?>> bindingNodeStack = new ArrayList<>(
				parentContext.nodeStack);
		bindingNodeStack.add(bindingNode);
		this.nodeStack = Collections.unmodifiableList(bindingNodeStack);
		bindings = parentContext.bindings;

		dataTypeCache = parentContext.dataTypeCache;
		modelCache = parentContext.modelCache;

		provider = parentContext.provider;
	}

	@Override
	public S copy() {
		return getThis();
	}

	private <T> ComputingMap<DataBindingType<? extends T>, DataNode.Effective<? extends T>> getDataNodeOverrideMap(
			DataNode.Effective<T> node) {
		List<DataBindingType<? extends T>> types = manager.registeredTypes()
				.getTypesWithBase(node).stream().map(n -> n.source())
				.collect(Collectors.toCollection(ArrayList::new));

		ComputingMap<DataBindingType<? extends T>, DataNode.Effective<? extends T>> overrideMap = new DeferredComputingMap<>(
				type -> getDataNodeOverride(node, type.effective()));
		overrideMap.putAll(types);

		return overrideMap;
	}

	private <T> DataNode.Effective<T> getDataNodeOverride(
			DataNode.Effective<? super T> node, DataBindingType.Effective<T> type) {
		return new BindingNodeOverrider().override(
				provisions().provide(DataBindingTypeBuilder.class), node, type);

	}

	@SuppressWarnings("unchecked")
	private <T> ComputingMap<Model<? extends T>, ComplexNode.Effective<? extends T>> getComplexNodeOverrideMap(
			ComplexNode.Effective<T> node) {
		List<Model<? extends T>> models;

		if (node.baseModel() != null && !node.baseModel().isEmpty()) {
			models = manager
					.registeredModels()
					.getModelsWithBase(node.baseModel())
					.stream()
					.map(SchemaNode::source)
					.filter(
							n -> node.getDataType().isAssignableFrom(
									n.effective().getDataType())).collect(Collectors.toList());
		} else {
			models = manager
					.registeredModels()
					.stream()
					.map(SchemaNode::source)
					.filter(
							c -> node.getDataType().isAssignableFrom(
									c.effective().getDataType()))
					.map(m -> (Model.Effective<? extends T>) m)
					.collect(Collectors.toList());
		}

		ComputingMap<Model<? extends T>, ComplexNode.Effective<? extends T>> overrideMap = new DeferredComputingMap<>(
				model -> getComplexNodeOverride(node, model.effective()));
		overrideMap.putAll(models);

		return overrideMap;
	}

	private <T> ComplexNode.Effective<T> getComplexNodeOverride(
			ComplexNode.Effective<? super T> node, Model.Effective<T> model) {
		return new BindingNodeOverrider().override(
				provisions().provide(ModelBuilder.class), node, model);
	}

	protected List<SchemaNode.Effective<?, ?>> nodeStack() {
		return new ArrayList<>(nodeStack);
	}

	public Bindings bindings() {
		return bindings;
	}

	protected ProcessingProvisions getProvider() {
		return provider;
	}

	protected <U> U provide(TypeToken<U> clazz, S state) {
		if (!provider.isProvided(clazz))
			throw processingException("Requested type '" + clazz
					+ "' is not provided by the unbinding context", state);
		return provider.provide(clazz, state);
	}

	protected abstract RuntimeException processingException(String message,
			S state);

	public Provisions provisions() {
		return new Provisions() {
			@Override
			public <U> U provide(TypeToken<U> clazz) {
				return ProcessingContextImpl.this.provide(clazz, getThis());
			}

			@Override
			public boolean isProvided(TypeToken<?> clazz) {
				return provider.isProvided(clazz);
			}
		};
	}

	public <T> S withProvision(Class<T> providedClass, Factory<T> provider) {
		return withProvision(TypeToken.over(providedClass), c -> provider.create());
	}

	public <T> S withProvision(TypeToken<T> providedClass, Factory<T> provider) {
		return withProvision(providedClass, c -> provider.create());
	}

	public <T> S withProvision(Class<T> providedClass,
			Function<? super S, T> provider) {
		return withProvision(TypeToken.over(providedClass), provider);
	}

	public <T> S withProvision(TypeToken<T> providedClass,
			Function<? super S, T> provider) {
		return withProvision(providedClass, provider, new ProcessingProvisions() {
			@SuppressWarnings("unchecked")
			@Override
			public <U> U provide(TypeToken<U> type, S headContext) {
				boolean canEqual = false;

				try {
					type.withEquality(providedClass);
					canEqual = true;
				} catch (Exception e) {}

				if (canEqual)
					return (U) provider.apply(headContext);

				return getThis().provide(type, headContext);
			}

			@Override
			public boolean isProvided(TypeToken<?> clazz) {
				return clazz.equals(providedClass)
						|| getThis().provisions().isProvided(clazz);
			}
		});
	}

	protected abstract <T> S withProvision(TypeToken<T> providedClass,
			Function<? super S, T> provider, ProcessingProvisions provisions);

	public Model.Effective<?> getModel(QualifiedName nextElement) {
		Model<?> model = manager.registeredModels().get(nextElement);
		return model == null ? null : model.effective();
	}

	public <U> List<Model.Effective<U>> getMatchingModels(TypeToken<U> dataClass) {
		return manager.registeredModels().getModelsWithClass(dataClass).stream()
				.map(n -> n.effective()).collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	public <T> ComputingMap<DataBindingType<? extends T>, DataNode.Effective<? extends T>> getDataNodeOverrides(
			DataNode<T> node) {
		return (ComputingMap<DataBindingType<? extends T>, DataNode.Effective<? extends T>>) dataTypeCache
				.putGet(node.source());
	}

	@SuppressWarnings("unchecked")
	public <T> ComputingMap<Model<? extends T>, ComplexNode.Effective<? extends T>> getComplexNodeOverrides(
			ComplexNode<T> node) {
		return (ComputingMap<Model<? extends T>, ComplexNode.Effective<? extends T>>) modelCache
				.putGet(node.source());
	}
}
