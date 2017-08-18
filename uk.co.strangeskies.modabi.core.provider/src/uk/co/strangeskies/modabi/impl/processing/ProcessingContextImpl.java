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

import static uk.co.strangeskies.modabi.processing.ProcessingException.MESSAGES;

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

import uk.co.strangeskies.collection.computingmap.ComputingMap;
import uk.co.strangeskies.collection.computingmap.DeferredComputingMap;
import uk.co.strangeskies.collection.computingmap.LRUCacheComputingMap;
import uk.co.strangeskies.modabi.ProcessedBindings;
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
import uk.co.strangeskies.modabi.io.structured.NavigableStructuredDataReader;
import uk.co.strangeskies.modabi.io.structured.StructuredDataBuffer;
import uk.co.strangeskies.modabi.io.structured.StructuredDataBuffer.Navigable;
import uk.co.strangeskies.modabi.io.structured.StructuredDataReader;
import uk.co.strangeskies.modabi.io.structured.StructuredDataWriter;
import uk.co.strangeskies.modabi.processing.BindingBlocker;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.StructuralNode;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypedObject;

public class ProcessingContextImpl implements ProcessingContext {
  private final SchemaManager manager;

  private final List<TypedObject<?>> objectStack;
  private final List<BindingPoint<?>> bindingStack;
  private final ProcessedBindings bindings; // TODO erase bindings in failed sections

  /*
   * Model and DataType caches
   */
  private final Function<StructuralNode<?>, ComputingMap<Model<?>, StructuralNode<?>>> modelCache;

  /*
   * Fetch model
   */
  private final Function<QualifiedName, Model<?>> getModel;

  private final Provisions provisions;

  private StructuredDataReader input;
  private StructuredDataWriter output;
  private final boolean exhaustive;

  private final BindingBlocker bindingFutureBlocker;

  /*
   * Registered models and types
   */
  private final Models registeredModels;

  public ProcessingContextImpl(SchemaManager manager) {
    this.manager = manager;

    objectStack = Collections.emptyList();
    bindingStack = Collections.emptyList();
    bindings = new ProcessedBindings();

    modelCache = new LRUCacheComputingMap<StructuralNode<?>, ComputingMap<Model<?>, StructuralNode<?>>>(
        node -> getComplexNodeOverrideMap(node),
        150,
        true)::putGet;

    getModel = name -> manager.registeredModels().get(name);

    provisions = manager.provisions();
    exhaustive = true;

    registeredModels = manager.registeredModels();

    bindingFutureBlocker = new BindingBlocksImpl();

    for (Model<?> schemaModel : manager.getMetaSchema().models()) {
      bindings().add(manager.getMetaSchema().getMetaModel(), schemaModel);
    }
  }

  public ProcessingContextImpl(ProcessingContext parentContext) {
    manager = parentContext.manager();

    objectStack = parentContext.getBindingObjectStack();
    bindingStack = parentContext.getBindingNodeStack();
    bindings = parentContext.bindings();

    modelCache = parentContext::getComplexNodeOverrides;

    getModel = parentContext::getModel;

    this.provisions = parentContext.provisions();
    this.exhaustive = parentContext.isExhaustive();

    this.input = parentContext.input().orElse(null);
    this.output = parentContext.output().orElse(null);

    registeredModels = parentContext.registeredModels();

    bindingFutureBlocker = parentContext.bindingBlocker();
  }

  protected ProcessingContextImpl(
      ProcessingContext parentContext,
      List<TypedObject<?>> objectStack,
      List<BindingPoint<?>> bindingStack,
      StructuredDataReader input,
      StructuredDataWriter output,
      Provisions provider,
      boolean exhaustive,
      BindingBlocker blocker) {
    manager = parentContext.manager();

    this.objectStack = objectStack;
    this.bindingStack = bindingStack;
    bindings = parentContext.bindings();

    modelCache = parentContext::getComplexNodeOverrides;

    getModel = parentContext::getModel;

    this.provisions = provider;
    this.exhaustive = exhaustive;

    this.input = input;
    this.output = output;

    registeredModels = parentContext.registeredModels();

    bindingFutureBlocker = blocker;
  }

  @Override
  public SchemaManager manager() {
    return manager;
  }

  @Override
  public List<BindingPoint<?>> getBindingNodeStack() {
    return bindingStack;
  }

  @Override
  public List<TypedObject<?>> getBindingObjectStack() {
    return objectStack;
  }

  @Override
  public Optional<StructuredDataReader> input() {
    return Optional.ofNullable(input);
  }

  @Override
  public Optional<StructuredDataWriter> output() {
    return Optional.ofNullable(output);
  }

  @SuppressWarnings("unchecked")
  private ComputingMap<Model<?>, StructuralNode<?>> getComplexNodeOverrideMap(StructuralNode<?> node) {
    List<Model<?>> models;

    if (node.baseNodes() != null && !node.baseNodes().isEmpty()) {
      models = registeredModels
          .getModelsWithBase(node.baseNodes())
          .stream()
          .filter(n -> node.getDataType().isAssignableFrom(n.getDataType()))
          .collect(Collectors.toList());
    } else {
      models = registeredModels
          .stream()
          .filter(c -> node.getDataType().isAssignableFrom(c.dataType()))
          .collect(Collectors.toList());
    }

    ComputingMap<Model<?>, StructuralNode> overrideMap = new DeferredComputingMap<>(
        model -> getComplexNodeOverride(node, model));
    overrideMap.putAll(models);

    return overrideMap;
  }

  private <T> StructuralNode getComplexNodeOverride(StructuralNode node, Model<?> model) {
    return new BindingNodeOverrider(provisions().provide(SchemaBuilder.class, this).getObject())
        .override(node, model);
  }

  @Override
  public ProcessedBindings bindings() {
    return bindings;
  }

  @Override
  public Provisions provisions() {
    return provisions;
  }

  public <T> ProcessingContextImpl withNestedProvisionScope() {
    return new ProcessingContextImpl(
        this,
        objectStack,
        bindingStack,
        input,
        output,
        provisions.nestChildScope(),
        exhaustive,
        bindingFutureBlocker);
  }

  @Override
  public Model<?> getModel(QualifiedName nextElement) {
    return getModel.apply(nextElement);
  }

  @Override
  public ComputingMap<Model<?>, StructuralNode<?>> getComplexNodeOverrides(StructuralNode<?> node) {
    return modelCache.apply(node);
  }

  @Override
  public BindingBlocker bindingBlocker() {
    return bindingFutureBlocker;
  }

  public ProcessingContextImpl withInput(StructuredDataReader input) {
    return new ProcessingContextImpl(
        this,
        objectStack,
        bindingStack,
        input,
        output,
        provisions,
        exhaustive,
        bindingFutureBlocker);
  }

  public ProcessingContextImpl withOutput(StructuredDataWriter output) {
    return new ProcessingContextImpl(
        this,
        objectStack,
        bindingStack,
        input,
        output,
        provisions,
        exhaustive,
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

    return new ProcessingContextImpl(
        this,
        Collections.unmodifiableList(bindingObjectStack),
        bindingStack,
        input,
        output,
        provisions,
        exhaustive,
        bindingFutureBlocker);
  }

  public <T> ProcessingContextImpl withBindingNode(BindingPoint<?> node) {
    return withBindingNode(node, false);
  }

  public <T> ProcessingContextImpl withReplacementBindingNode(BindingPoint<?> node) {
    return withBindingNode(node, true);
  }

  public <T> ProcessingContextImpl withBindingNode(BindingPoint<?> node, boolean replace) {
    List<BindingPoint<?>> nodeStack = new ArrayList<>(this.bindingStack);
    if (replace && !nodeStack.isEmpty()) {
      nodeStack.set(nodeStack.size() - 1, node);
    } else {
      nodeStack.add(node);
    }

    return new ProcessingContextImpl(
        this,
        objectStack,
        Collections.unmodifiableList(nodeStack),
        input,
        output,
        provisions,
        exhaustive,
        bindingFutureBlocker);
  }

  public void attemptUnbinding(Consumer<ProcessingContextImpl> unbindingMethod) {
    ProcessingContextImpl context = this;

    BufferingDataTarget dataTarget = null;

    Navigable output = StructuredDataBuffer.singleBuffer(this.output.index()).addChild(
        new QualifiedName(""));

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

    NavigableStructuredDataReader bufferedData = output.endChild().getBuffer();
    bufferedData.readNextChild();
    bufferedData.pipeDataAtChild(this.output);
    bufferedData.pipeNextChild(this.output);
  }

  public <I> I attemptUnbindingUntilSuccessful(
      Iterable<I> attemptItems,
      BiConsumer<ProcessingContextImpl, I> unbindingMethod) {
    if (!attemptItems.iterator().hasNext())
      throw new ProcessingException(MESSAGES.mustSupplyAttemptItems(), this);

    Set<Exception> failures = new HashSet<>();

    for (I item : attemptItems)
      try {
        attemptUnbinding(c -> unbindingMethod.accept(c, item));

        return item;
      } catch (Exception e) {
        failures.add(e);
      }

    throw ProcessingException.mergeExceptions(this, failures);
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
    StructuredDataReader input = this.input.split();
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

  public <I> I attemptBindingUntilSuccessful(
      Iterable<I> attemptItems,
      BiConsumer<ProcessingContextImpl, I> bindingMethod,
      Function<Set<Exception>, ProcessingException> onFailure) {
    if (!attemptItems.iterator().hasNext())
      throw new ProcessingException(MESSAGES.mustSupplyAttemptItems(), this);

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
    return new ProcessingContextImpl(
        this,
        objectStack,
        bindingStack,
        input,
        output,
        provisions,
        isExhaustive() && exhaustive,
        bindingFutureBlocker);
  }

  public ProcessingContextImpl forceExhausting() {
    return new ProcessingContextImpl(
        this,
        objectStack,
        bindingStack,
        input,
        output,
        provisions,
        true,
        bindingFutureBlocker);
  }

  @Override
  public Models registeredModels() {
    return registeredModels;
  }
}
