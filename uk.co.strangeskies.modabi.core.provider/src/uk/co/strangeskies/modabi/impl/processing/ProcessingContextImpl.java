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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.Bindings;
import uk.co.strangeskies.modabi.DataTypes;
import uk.co.strangeskies.modabi.Models;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.Provisions;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.impl.BindingNodeOverrider;
import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.io.structured.NavigableStructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataBuffer;
import uk.co.strangeskies.modabi.io.structured.StructuredDataBuffer.Navigable;
import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.processing.BindingBlocker;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaNode.Effective;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypedObject;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;
import uk.co.strangeskies.utilities.collection.computingmap.DeferredComputingMap;
import uk.co.strangeskies.utilities.collection.computingmap.LRUCacheComputingMap;

public class ProcessingContextImpl implements ProcessingContext {
	private final SchemaManager manager;

	private final List<TypedObject<?>> objectStack;
	private final List<SchemaNode.Effective<?, ?>> nodeStack;
	private final Bindings bindings; // TODO erase bindings in failed sections

	/*
	 * Model and DataType caches
	 */
	private final Function<DataNode<?>, ComputingMap<? extends DataType<?>, ? extends DataNode.Effective<?>>> dataTypeCache;
	private final Function<ComplexNode<?>, ComputingMap<? extends Model<?>, ? extends ComplexNode.Effective<?>>> modelCache;

	/*
	 * Fetch model
	 */
	private final Function<QualifiedName, Model.Effective<?>> getModel;

	private final Provisions provisions;

	private StructuredDataSource input;
	private StructuredDataTarget output;
	private final boolean exhaustive;

	private final BindingBlocker bindingFutureBlocker;

	/*
	 * Registered models and types
	 */
	private final Models registeredModels;
	private final DataTypes registeredTypes;

	public ProcessingContextImpl(SchemaManager manager) {
		this.manager = manager;

		objectStack = Collections.emptyList();
		nodeStack = Collections.emptyList();
		bindings = new Bindings();

		dataTypeCache = new LRUCacheComputingMap<DataNode<?>, ComputingMap<? extends DataType<?>, ? extends DataNode.Effective<?>>>(
				node -> getDataNodeOverrideMap(node.effective()), 150, true)::putGet;

		modelCache = new LRUCacheComputingMap<ComplexNode<?>, ComputingMap<? extends Model<?>, ? extends ComplexNode.Effective<?>>>(
				node -> getComplexNodeOverrideMap(node.effective()), 150, true)::putGet;

		getModel = name -> {
			Model<?> model = manager.registeredModels().get(name);
			return model == null ? null : model.effective();
		};

		provisions = manager.provisions();
		exhaustive = true;

		registeredModels = manager.registeredModels();
		registeredTypes = manager.registeredTypes();

		bindingFutureBlocker = new BindingBlocksImpl();
	}

	public ProcessingContextImpl(ProcessingContext parentContext) {
		manager = parentContext.manager();

		objectStack = parentContext.getBindingObjectStack();
		nodeStack = parentContext.getBindingNodeStack();
		bindings = parentContext.bindings();

		dataTypeCache = parentContext::getDataNodeOverrides;
		modelCache = parentContext::getComplexNodeOverrides;

		getModel = parentContext::getModel;

		this.provisions = parentContext.provisions();
		this.exhaustive = parentContext.isExhaustive();

		this.input = parentContext.input().orElse(null);
		this.output = parentContext.output().orElse(null);

		registeredModels = parentContext.registeredModels();
		registeredTypes = parentContext.registeredTypes();

		bindingFutureBlocker = parentContext.bindingFutureBlocker();
	}

	protected ProcessingContextImpl(ProcessingContext parentContext, List<TypedObject<?>> objectStack,
			List<SchemaNode.Effective<?, ?>> nodeStack, StructuredDataSource input, StructuredDataTarget output,
			Provisions provider, boolean exhaustive, BindingBlocker blocker) {
		manager = parentContext.manager();

		this.objectStack = objectStack;
		this.nodeStack = nodeStack;
		bindings = parentContext.bindings();

		dataTypeCache = parentContext::getDataNodeOverrides;
		modelCache = parentContext::getComplexNodeOverrides;

		getModel = parentContext::getModel;

		this.provisions = provider;
		this.exhaustive = exhaustive;

		this.input = input;
		this.output = output;

		registeredModels = parentContext.registeredModels();
		registeredTypes = parentContext.registeredTypes();

		bindingFutureBlocker = blocker;
	}

	@Override
	public SchemaManager manager() {
		return manager;
	}

	@Override
	public List<Effective<?, ?>> getBindingNodeStack() {
		return nodeStack;
	}

	@Override
	public List<TypedObject<?>> getBindingObjectStack() {
		return objectStack;
	}

	@Override
	public Optional<StructuredDataSource> input() {
		return Optional.ofNullable(input);
	}

	@Override
	public Optional<StructuredDataTarget> output() {
		return Optional.ofNullable(output);
	}

	private <T> ComputingMap<DataType<? extends T>, DataNode.Effective<? extends T>> getDataNodeOverrideMap(
			DataNode.Effective<T> node) {
		List<DataType<? extends T>> types = registeredTypes.getTypesWithBase(node).stream().map(n -> n.source())
				.collect(Collectors.toCollection(ArrayList::new));

		ComputingMap<DataType<? extends T>, DataNode.Effective<? extends T>> overrideMap = new DeferredComputingMap<>(
				type -> getDataNodeOverride(node, type.effective()));
		overrideMap.putAll(types);

		return overrideMap;
	}

	private <T> DataNode.Effective<T> getDataNodeOverride(DataNode.Effective<? super T> node,
			DataType.Effective<T> type) {
		return new BindingNodeOverrider().override(provisions().provide(SchemaBuilder.class, this).getObject(), node, type);
	}

	@SuppressWarnings("unchecked")
	private <T> ComputingMap<Model<? extends T>, ComplexNode.Effective<? extends T>> getComplexNodeOverrideMap(
			ComplexNode.Effective<T> node) {
		List<Model<? extends T>> models;

		if (node.model() != null && !node.model().isEmpty()) {
			models = registeredModels.getModelsWithBase(node.model()).stream().map(SchemaNode::source)
					.filter(n -> node.getDataType().isAssignableFrom(n.effective().getDataType())).collect(Collectors.toList());
		} else {
			models = registeredModels.stream().map(SchemaNode::source)
					.filter(c -> node.getDataType().isAssignableFrom(c.effective().getDataType()))
					.map(m -> (Model.Effective<? extends T>) m.effective()).collect(Collectors.toList());
		}

		ComputingMap<Model<? extends T>, ComplexNode.Effective<? extends T>> overrideMap = new DeferredComputingMap<>(
				model -> getComplexNodeOverride(node, model.effective()));
		overrideMap.putAll(models);

		return overrideMap;
	}

	private <T> ComplexNode.Effective<T> getComplexNodeOverride(ComplexNode.Effective<? super T> node,
			Model.Effective<T> model) {
		return new BindingNodeOverrider().override(provisions().provide(SchemaBuilder.class, this).getObject(), node,
				model);
	}

	@Override
	public Bindings bindings() {
		return bindings;
	}

	@Override
	public Provisions provisions() {
		return provisions;
	}

	public <T> ProcessingContextImpl withNestedProvisionScope() {
		return new ProcessingContextImpl(this, objectStack, nodeStack, input, output, provisions.nestChildScope(),
				exhaustive, bindingFutureBlocker);
	}

	@Override
	public Model.Effective<?> getModel(QualifiedName nextElement) {
		return getModel.apply(nextElement);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> ComputingMap<DataType<? extends T>, DataNode.Effective<? extends T>> getDataNodeOverrides(
			DataNode<T> node) {
		return (ComputingMap<DataType<? extends T>, DataNode.Effective<? extends T>>) dataTypeCache.apply(node.source());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> ComputingMap<Model<? extends T>, ComplexNode.Effective<? extends T>> getComplexNodeOverrides(
			ComplexNode<T> node) {
		return (ComputingMap<Model<? extends T>, ComplexNode.Effective<? extends T>>) modelCache.apply(node.source());
	}

	@Override
	public BindingBlocker bindingFutureBlocker() {
		return bindingFutureBlocker;
	}

	public ProcessingContextImpl withInput(StructuredDataSource input) {
		return new ProcessingContextImpl(this, objectStack, nodeStack, input, output, provisions, exhaustive,
				bindingFutureBlocker);
	}

	public ProcessingContextImpl withOutput(StructuredDataTarget output) {
		return new ProcessingContextImpl(this, objectStack, nodeStack, input, output, provisions, exhaustive,
				bindingFutureBlocker);
	}

	public <T> ProcessingContextImpl withBindingObject(TypedObject<?> target) {
		return withBindingObject(target, false);
	}

	public <T> ProcessingContextImpl withReplacementBindingObject(TypedObject<?> target) {
		return withBindingObject(target, true);
	}

	public <T> ProcessingContextImpl withBindingObject(TypedObject<?> target, boolean replace) {
		List<TypedObject<?>> bindingObjectStack = new ArrayList<>(objectStack);
		if (replace) {
			bindingObjectStack.set(bindingObjectStack.size() - 1, target);
		} else {
			bindingObjectStack.add(target);
		}

		return new ProcessingContextImpl(this, Collections.unmodifiableList(bindingObjectStack), nodeStack, input, output,
				provisions, exhaustive, bindingFutureBlocker);
	}

	public <T> ProcessingContextImpl withBindingNode(SchemaNode.Effective<?, ?> node) {
		return withBindingNode(node, false);
	}

	public <T> ProcessingContextImpl withReplacementBindingNode(SchemaNode.Effective<?, ?> node) {
		return withBindingNode(node, true);
	}

	public <T> ProcessingContextImpl withBindingNode(SchemaNode.Effective<?, ?> node, boolean replace) {
		List<SchemaNode.Effective<?, ?>> nodeStack = new ArrayList<>(this.nodeStack);
		if (replace && !nodeStack.isEmpty()) {
			nodeStack.set(nodeStack.size() - 1, node);
		} else {
			nodeStack.add(node);
		}

		return new ProcessingContextImpl(this, objectStack, Collections.unmodifiableList(nodeStack), input, output,
				provisions, exhaustive, bindingFutureBlocker);
	}

	public void attemptUnbinding(Consumer<ProcessingContextImpl> unbindingMethod) {
		ProcessingContextImpl context = this;

		BufferingDataTarget dataTarget = null;

		Navigable output = StructuredDataBuffer.singleBuffer(this.output.index()).addChild(new QualifiedName(""));

		/*
		 * Mark output! (by redirecting to a new buffer)
		 */
		if (context.provisions().isProvided(DataTarget.class, this)) {
			dataTarget = new BufferingDataTarget();
			DataTarget finalTarget = dataTarget;
			context = context.withNestedProvisionScope();
			context.provisions().add(Provider.over(new TypeToken<DataTarget>() {}, () -> finalTarget));
		}
		context = context.withOutput(output);

		/*
		 * Make unbinding attempt! (Reset output to mark on failure by discarding
		 * buffer, via exception.)
		 */
		unbindingMethod.accept(context);

		/*
		 * Remove mark! (by flushing buffer into output)
		 */
		if (dataTarget != null)
			dataTarget.buffer().pipe(provisions().provide(DataTarget.class, this).getObject());

		NavigableStructuredDataSource bufferedData = output.endChild().getBuffer();
		bufferedData.startNextChild();
		bufferedData.pipeDataAtChild(this.output);
		bufferedData.pipeNextChild(this.output);
	}

	public <I> I attemptUnbindingUntilSuccessful(Iterable<I> attemptItems,
			BiConsumer<ProcessingContextImpl, I> unbindingMethod, Function<Set<Exception>, ProcessingException> onFailure) {
		if (!attemptItems.iterator().hasNext())
			throw new ProcessingException("Must supply items for unbinding attempt", this);

		Set<Exception> failures = new HashSet<>();

		for (I item : attemptItems)
			try {
				attemptUnbinding(c -> unbindingMethod.accept(c, item));

				return item;
			} catch (Exception e) {
				failures.add(e);
			}

		throw onFailure.apply(failures);
	}

	public void attemptBinding(Consumer<ProcessingContextImpl> bindingMethod) {
		attemptBinding((Function<ProcessingContextImpl, Void>) c -> {
			bindingMethod.accept(c);
			return null;
		});
	}

	public <U> U attemptBinding(Function<ProcessingContextImpl, U> bindingMethod) {
		ProcessingContextImpl context = this;
		DataSource dataSource = null;

		/*
		 * Mark output! (by redirecting to a new buffer)
		 */
		if (context.provisions().isProvided(DataSource.class, this)) {
			dataSource = context.provisions().provide(DataSource.class, this).getObject().copy();
			DataSource finalSource = dataSource;
			context = context.withNestedProvisionScope();
			context.provisions().add(Provider.over(new TypeToken<DataSource>() {}, () -> finalSource));
		}
		StructuredDataSource input = this.input.split();
		context = context.withInput(input);

		/*
		 * Make unbinding attempt! (Reset output to mark on failure by discarding
		 * buffer, via exception.)
		 */
		U result = bindingMethod.apply(context);

		/*
		 * Remove mark! (by flushing buffer into output)
		 */
		if (dataSource != null) {
			DataSource originalDataSource = provisions().provide(DataSource.class, this).getObject();
			while (originalDataSource.index() < dataSource.index())
				originalDataSource.get();
		}

		this.input = input;

		return result;
	}

	public <I> I attemptBindingUntilSuccessful(Iterable<I> attemptItems,
			BiConsumer<ProcessingContextImpl, I> bindingMethod, Function<Set<Exception>, ProcessingException> onFailure) {
		if (!attemptItems.iterator().hasNext())
			throw new ProcessingException("Must supply items for binding attempt", this);

		Set<Exception> failures = new HashSet<>();

		for (I item : attemptItems)
			try {
				attemptBinding((Consumer<ProcessingContextImpl>) c -> bindingMethod.accept(c, item));

				return item;
			} catch (Exception e) {
				failures.add(e);
			}

		throw onFailure.apply(failures);
	}

	@Override
	public boolean isExhaustive() {
		return exhaustive;
	}

	public ProcessingContextImpl exhausting(boolean exhaustive) {
		/*
		 * TODO actually make non-exhausting nodes non-exhausting...
		 */
		return new ProcessingContextImpl(this, objectStack, nodeStack, input, output, provisions,
				isExhaustive() && exhaustive, bindingFutureBlocker);
	}

	public ProcessingContextImpl forceExhausting() {
		return new ProcessingContextImpl(this, objectStack, nodeStack, input, output, provisions, true,
				bindingFutureBlocker);
	}

	@Override
	public Models registeredModels() {
		return registeredModels;
	}

	@Override
	public DataTypes registeredTypes() {
		return registeredTypes;
	}
}
