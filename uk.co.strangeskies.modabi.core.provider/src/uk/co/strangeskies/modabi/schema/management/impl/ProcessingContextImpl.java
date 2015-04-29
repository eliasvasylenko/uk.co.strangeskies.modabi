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
package uk.co.strangeskies.modabi.schema.management.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.Bindings;
import uk.co.strangeskies.modabi.schema.management.Provisions;
import uk.co.strangeskies.modabi.schema.management.SchemaManager;
import uk.co.strangeskies.modabi.schema.node.ComplexNode;
import uk.co.strangeskies.modabi.schema.node.DataBindingType;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.Model;
import uk.co.strangeskies.modabi.schema.node.Model.Effective;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.building.DataBindingTypeBuilder;
import uk.co.strangeskies.modabi.schema.node.building.ModelBuilder;
import uk.co.strangeskies.modabi.schema.node.wrapping.impl.ComplexNodeWrapper;
import uk.co.strangeskies.modabi.schema.node.wrapping.impl.DataNodeWrapper;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;
import uk.co.strangeskies.utilities.collection.computingmap.DeferredComputingMap;
import uk.co.strangeskies.utilities.collection.computingmap.LRUCacheComputingMap;

public abstract class ProcessingContextImpl {
	public enum CacheScope {
		MANAGER_GLOBAL, PROCESSING_CONTEXT
	}

	private final SchemaManager manager;

	private final List<SchemaNode.Effective<?, ?>> bindingNodeStack;
	private final Bindings bindings; // TODO erase bindings in failed sections

	private final ComputingMap<DataNode.Effective<?>, ComputingMap<? extends DataBindingType.Effective<?>, ? extends DataNode.Effective<?>>> dataTypeCache;
	private final ComputingMap<ComplexNode.Effective<?>, ComputingMap<? extends Model.Effective<?>, ? extends ComplexNode.Effective<?>>> modelCache;

	public ProcessingContextImpl(SchemaManager manager) {
		this.manager = manager;

		bindingNodeStack = Collections.emptyList();
		bindings = new Bindings();

		dataTypeCache = new LRUCacheComputingMap<>(
				node -> getDataNodeOverrideMap(node), 150, true);

		modelCache = new LRUCacheComputingMap<>(
				node -> getComplexNodeOverrideMap(node), 150, true);
	}

	public ProcessingContextImpl(ProcessingContextImpl parentContext) {
		manager = parentContext.manager;

		bindingNodeStack = parentContext.bindingNodeStack;
		bindings = parentContext.bindings;

		dataTypeCache = parentContext.dataTypeCache;
		modelCache = parentContext.modelCache;
	}

	public ProcessingContextImpl(ProcessingContextImpl parentContext,
			SchemaNode.Effective<?, ?> bindingNode) {
		manager = parentContext.manager;

		List<SchemaNode.Effective<?, ?>> bindingNodeStack = new ArrayList<>(
				parentContext.bindingNodeStack);
		bindingNodeStack.add(bindingNode);
		this.bindingNodeStack = Collections.unmodifiableList(bindingNodeStack);
		bindings = parentContext.bindings;

		dataTypeCache = parentContext.dataTypeCache;
		modelCache = parentContext.modelCache;
	}

	private <T> ComputingMap<DataBindingType.Effective<? extends T>, DataNode.Effective<? extends T>> getDataNodeOverrideMap(
			DataNode.Effective<T> node) {
		List<DataBindingType.Effective<? extends T>> types = manager
				.registeredTypes().getTypesWithBase(node).stream()
				.map(n -> n.effective())
				.collect(Collectors.toCollection(ArrayList::new));

		ComputingMap<DataBindingType.Effective<? extends T>, DataNode.Effective<? extends T>> overrideMap = new DeferredComputingMap<>(
				type -> getDataNodeOverride(node, type));
		overrideMap.putAll(types);

		return overrideMap;
	}

	private <T> DataNode.Effective<T> getDataNodeOverride(
			DataNode.Effective<? super T> node, DataBindingType.Effective<T> type) {
		if (node.children().isEmpty())
			return new DataNodeWrapper<>(type, node);
		else
			return new BindingNodeOverrider().override(
					provisions().provide(DataBindingTypeBuilder.class), node, type);

	}

	@SuppressWarnings("unchecked")
	private <T> ComputingMap<Model.Effective<? extends T>, ComplexNode.Effective<? extends T>> getComplexNodeOverrideMap(
			ComplexNode.Effective<T> node) {
		List<Model.Effective<? extends T>> models;

		if (node.baseModel() != null && !node.baseModel().isEmpty())
			models = manager
					.registeredModels()
					.getModelsWithBase(node.baseModel())
					.stream()
					.map(SchemaNode::effective)
					.filter(
							n -> TypeToken.over(node.getDataType().getType()).isAssignableFrom(
									TypeToken.over(n.getDataType().getType())))
					.collect(Collectors.toList());
		else
			models = manager
					.registeredModels()
					.stream()
					.map(SchemaNode::effective)
					.filter(
							c -> TypeToken.over(node.getDataType().getType()).isAssignableFrom(
									c.getDataType().getType()))
					.map(m -> (Model.Effective<? extends T>) m)
					.collect(Collectors.toList());

		ComputingMap<Model.Effective<? extends T>, ComplexNode.Effective<? extends T>> overrideMap = new DeferredComputingMap<>(
				model -> getComplexNodeOverride(node, model));
		overrideMap.putAll(models);

		return overrideMap;
	}

	private <T> ComplexNode.Effective<T> getComplexNodeOverride(
			ComplexNode.Effective<? super T> node, Model.Effective<T> model) {
		if (node.children().isEmpty())
			return new ComplexNodeWrapper<>(model, node);
		else
			return new BindingNodeOverrider().override(
					provisions().provide(ModelBuilder.class), node, model);
	}

	public List<SchemaNode.Effective<?, ?>> bindingNodeStack() {
		return bindingNodeStack;
	}

	public Bindings bindings() {
		return bindings;
	}

	public abstract Provisions provisions();

	public Model.Effective<?> getModel(QualifiedName nextElement) {
		Model<?> model = manager.registeredModels().get(nextElement);
		return model == null ? null : model.effective();
	}

	public <U> List<Model.Effective<U>> getMatchingModels(TypeToken<U> dataClass) {
		return manager.registeredModels().getModelsWithClass(dataClass).stream()
				.map(n -> n.effective()).collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	public <T> ComputingMap<DataBindingType.Effective<? extends T>, DataNode.Effective<? extends T>> getDataNodeOverrides(
			DataNode.Effective<T> node) {
		return (ComputingMap<DataBindingType.Effective<? extends T>, DataNode.Effective<? extends T>>) dataTypeCache
				.putGet(node);
	}

	@SuppressWarnings("unchecked")
	public <T> ComputingMap<Model.Effective<? extends T>, ComplexNode.Effective<? extends T>> getComplexNodeOverrides(
			ComplexNode.Effective<T> node) {
		return (ComputingMap<Effective<? extends T>, ComplexNode.Effective<? extends T>>) modelCache
				.putGet(node);
	}
}
