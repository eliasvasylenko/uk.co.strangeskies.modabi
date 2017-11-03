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
package uk.co.strangeskies.modabi.binding.impl;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static uk.co.strangeskies.modabi.binding.BindingException.MESSAGES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import uk.co.strangeskies.modabi.Bindings;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.binding.BindingContext;
import uk.co.strangeskies.modabi.binding.BindingException;
import uk.co.strangeskies.modabi.binding.Provisions;
import uk.co.strangeskies.modabi.io.structured.NavigableStructuredDataReader;
import uk.co.strangeskies.modabi.io.structured.StructuredDataBuffer;
import uk.co.strangeskies.modabi.io.structured.StructuredDataReader;
import uk.co.strangeskies.modabi.io.structured.StructuredDataWriter;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypedObject;

public class BindingContextImpl implements BindingContext {
  private final SchemaManager manager;

  private final List<TypedObject<?>> objectStack;
  private final List<BindingPoint<?>> bindingStack;
  private final Set<Provider> providers;
  private final Bindings localBindings;
  private final Bindings globalBindings;

  private StructuredDataReader input;
  private StructuredDataWriter output;

  public BindingContextImpl(SchemaManager manager) {
    this.manager = manager;

    objectStack = Collections.emptyList();
    bindingStack = Collections.emptyList();
    providers = Collections.emptySet();
    localBindings = new Bindings();
    globalBindings = new Bindings();
  }

  protected BindingContextImpl(
      BindingContext parentContext,
      List<TypedObject<?>> objectStack,
      List<BindingPoint<?>> bindingStack,
      Set<Provider> providers,
      StructuredDataReader input,
      StructuredDataWriter output) {
    this.manager = parentContext.manager();

    this.objectStack = objectStack;
    this.bindingStack = bindingStack;
    this.providers = providers;
    this.localBindings = parentContext.localBindings(); // TODO erase bindings in failed sections
    this.globalBindings = parentContext.globalBindings();

    this.input = input;
    this.output = output;
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

  public Set<Provider> getProviders() {
    return providers;
  }

  @Override
  public Provisions provisions() {
    return new ProvisionsImpl(this);
  }

  public <T> BindingContextImpl withProvider(Provider provider) {
    Set<Provider> providers = new LinkedHashSet<>(this.providers);
    providers.add(provider);
    return new BindingContextImpl(
        this,
        objectStack,
        bindingStack,
        unmodifiableSet(providers),
        input,
        output);
  }

  @Override
  public Bindings localBindings() {
    return localBindings;
  }

  @Override
  public Bindings globalBindings() {
    return globalBindings;
  }

  public BindingContextImpl withInput(StructuredDataReader input) {
    return new BindingContextImpl(this, objectStack, bindingStack, providers, input, output);
  }

  public BindingContextImpl withOutput(StructuredDataWriter output) {
    return new BindingContextImpl(this, objectStack, bindingStack, providers, input, output);
  }

  public <T> BindingContextImpl withBindingObject(TypedObject<?> target) {
    return withBindingObject(target, false);
  }

  public <T> BindingContextImpl withReplacementBindingObject(TypedObject<?> target) {
    return withBindingObject(target, true);
  }

  public <T> BindingContextImpl withBindingObject(TypedObject<?> target, boolean replace) {
    List<TypedObject<?>> bindingObjectStack = new ArrayList<>(objectStack);
    if (replace) {
      bindingObjectStack.set(bindingObjectStack.size() - 1, target);
    } else {
      bindingObjectStack.add(target);
    }

    return new BindingContextImpl(
        this,
        unmodifiableList(bindingObjectStack),
        bindingStack,
        providers,
        input,
        output);
  }

  public <T> BindingContextImpl withBindingNode(BindingPoint<?> node) {
    return withBindingNode(node, false);
  }

  public <T> BindingContextImpl withReplacementBindingNode(BindingPoint<?> node) {
    return withBindingNode(node, true);
  }

  public <T> BindingContextImpl withBindingNode(BindingPoint<?> node, boolean replace) {
    List<BindingPoint<?>> nodeStack = new ArrayList<>(this.bindingStack);
    if (replace && !nodeStack.isEmpty()) {
      nodeStack.set(nodeStack.size() - 1, node);
    } else {
      nodeStack.add(node);
    }

    return new BindingContextImpl(
        this,
        objectStack,
        unmodifiableList(nodeStack),
        providers,
        input,
        output);
  }

  public void attemptUnbinding(Consumer<BindingContextImpl> unbindingMethod) {
    BindingContextImpl context = this;

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
      BiConsumer<BindingContextImpl, I> unbindingMethod) {
    if (!attemptItems.iterator().hasNext())
      throw new BindingException(MESSAGES.mustSupplyAttemptItems(), this);

    Set<Exception> failures = new HashSet<>();

    for (I item : attemptItems)
      try {
        attemptUnbinding(c -> unbindingMethod.accept(c, item));

        return item;
      } catch (Exception e) {
        failures.add(e);
      }

    throw BindingException.mergeExceptions(this, failures);
  }

  public void attemptBinding(Consumer<BindingContextImpl> bindingMethod) {
    attemptBinding((Function<BindingContextImpl, Void>) c -> {
      bindingMethod.accept(c);
      return null;
    });
  }

  public <U> U attemptBinding(Function<BindingContextImpl, U> bindingMethod) {
    BindingContextImpl context = this;
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
      BiConsumer<BindingContextImpl, I> bindingMethod,
      Function<Set<Exception>, BindingException> onFailure) {
    if (!attemptItems.iterator().hasNext())
      throw new BindingException(MESSAGES.mustSupplyAttemptItems(), this);

    Set<Exception> failures = new HashSet<>();

    for (I item : attemptItems)
      try {
        attemptBinding((Consumer<BindingContextImpl>) c -> bindingMethod.accept(c, item));

        return item;
      } catch (Exception e) {
        failures.add(e);
      }

    throw onFailure.apply(failures);
  }

  public void cancel() {
    // TODO Auto-generated method stub

  }
}
