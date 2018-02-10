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
import static uk.co.strangeskies.reflection.token.TypedObject.typedObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.binding.BindingException;
import uk.co.strangeskies.modabi.binding.Provider;
import uk.co.strangeskies.modabi.io.StructuredDataReader;
import uk.co.strangeskies.modabi.io.StructuredDataWriter;
import uk.co.strangeskies.modabi.schema.BindingContext;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.Schemata;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypedObject;

public class BindingContextImpl implements BindingContext {
  private final Schemata schemata;

  private final List<TypedObject<?>> objectStack;
  private final List<BindingPoint<?>> bindingStack;
  private final Set<Provider> providers;

  private StructuredDataReader input;
  private StructuredDataWriter output;

  public BindingContextImpl(Schemata schemata) {
    this.schemata = schemata;

    objectStack = Collections.emptyList();
    bindingStack = Collections.emptyList();
    providers = Collections.emptySet();
  }

  protected BindingContextImpl(
      BindingContextImpl parentContext,
      List<TypedObject<?>> objectStack,
      List<BindingPoint<?>> bindingStack,
      Set<Provider> providers,
      StructuredDataReader input,
      StructuredDataWriter output) {
    this.schemata = parentContext.schemata;

    this.objectStack = objectStack;
    this.bindingStack = bindingStack;
    this.providers = providers;

    this.input = input;
    this.output = output;
  }

  @Override
  public TypedObject<?> getBindingObject() {
    return objectStack.get(objectStack.size() - 1);
  }

  @Override
  public BindingPoint<?> getBindingPoint() {
    return bindingStack.get(bindingStack.size() - 1);
  }

  public Schemata schemata() {
    return schemata;
  }

  public Optional<StructuredDataReader> input() {
    return Optional.ofNullable(input);
  }

  public Optional<StructuredDataWriter> output() {
    return Optional.ofNullable(output);
  }

  @Override
  public <T> TypedObject<T> provide(TypeToken<T> type) {
    return typedObject(
        type,
        providers
            .stream()
            .map(p -> p.provide(type, this))
            .filter(Objects::nonNull)
            .findFirst()
            .<ModabiException>orElseThrow(
                () -> new BindingException(MESSAGES.noProviderFound(type), this)));
  }

  @Override
  public boolean isProvided(TypeToken<?> type) {
    return providers.stream().map(p -> p.provide(type, this)).anyMatch(Objects::nonNull);
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
}
