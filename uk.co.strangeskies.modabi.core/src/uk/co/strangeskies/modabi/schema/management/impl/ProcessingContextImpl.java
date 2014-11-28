package uk.co.strangeskies.modabi.schema.management.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.Bindings;
import uk.co.strangeskies.modabi.schema.TypeLiteral;
import uk.co.strangeskies.modabi.schema.management.Provisions;
import uk.co.strangeskies.modabi.schema.management.SchemaManager;
import uk.co.strangeskies.modabi.schema.node.ComplexNode;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.node.model.Model.Effective;
import uk.co.strangeskies.modabi.schema.node.model.ModelBuilder;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingTypeBuilder;
import uk.co.strangeskies.modabi.schema.node.wrapping.impl.ComplexNodeWrapper;
import uk.co.strangeskies.modabi.schema.node.wrapping.impl.DataNodeWrapper;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;
import uk.co.strangeskies.utilities.collection.computingmap.DeferredComputingMap;
import uk.co.strangeskies.utilities.collection.computingmap.LRUCacheComputingMap;

public abstract class ProcessingContextImpl {
	public enum CacheScope {
		MANAGER_GLOBAL, PROCESSING_CONTEXT
	}

	private final SchemaManager manager;

	private final List<SchemaNode.Effective<?, ?>> bindingNodeStack;
	private final Bindings bindings;// TODO erase bindings in failed sections

	private final ComputingMap<DataNode.Effective<?>, ComputingMap<? extends DataBindingType.Effective<?>, ? extends DataNode.Effective<?>>> dataTypeCache;
	private final ComputingMap<ComplexNode.Effective<?>, ComputingMap<? extends Model.Effective<?>, ? extends ComplexNode.Effective<?>>> modelCache;

	@SuppressWarnings("unchecked")
	public ProcessingContextImpl(SchemaManager manager) {
		this.manager = manager;

		bindingNodeStack = Collections.emptyList();
		bindings = new Bindings();

		dataTypeCache = new LRUCacheComputingMap<>(
				node -> {
					List<DataBindingType<?>> types = manager.registeredTypes()
							.getTypesWithBase(node).stream().map(n -> n.effective())
							.collect(Collectors.toCollection(ArrayList::new));

					ComputingMap<DataBindingType.Effective<?>, DataNode.Effective<?>> overrideMap = new DeferredComputingMap<>(
							type -> {
								if (node.children().isEmpty())
									return new DataNodeWrapper<>(type,
											(DataNode.Effective<Object>) node);
								else
									return new BindingNodeOverrider().override(provisions()
											.provide(DataBindingTypeBuilder.class),
											(DataNode.Effective<Object>) node, type);
							});
					overrideMap
							.putAll((Collection<? extends DataBindingType.Effective<?>>) types);

					return overrideMap;
				}, 150, true);

		modelCache = new LRUCacheComputingMap<>(
				node -> {
					List<Model<?>> models = manager.registeredModels()
							.getCompatibleModels(node).stream().map(n -> n.effective())
							.collect(Collectors.toList());

					ComputingMap<Model.Effective<?>, ComplexNode.Effective<?>> overrideMap = new DeferredComputingMap<>(
							model -> {
								if (node.children().isEmpty())
									return new ComplexNodeWrapper<>(model,
											(ComplexNode.Effective<Object>) node);
								else
									return new BindingNodeOverrider().override(provisions()
											.provide(ModelBuilder.class),
											(ComplexNode.Effective<Object>) node, model);
							});
					overrideMap.putAll((Collection<? extends Model.Effective<?>>) models);

					return overrideMap;
				}, 150, true);
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

	public <U> List<Model.Effective<U>> getMatchingModels(TypeLiteral<U> dataClass) {
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
