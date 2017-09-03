/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.api.
 *
 * uk.co.strangeskies.modabi.core.api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.api.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.schema;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import uk.co.strangeskies.modabi.schema.expression.ValueExpression;
import uk.co.strangeskies.reflection.token.TypedObject;

/**
 * 
 * @author Elias N Vasylenko
 *
 * @param <T,
 *          E> the data type of the node, as defined by the binding point it
 *          attaches to
 * @param <E>
 *          the end product of the configurator, which will either be the parent
 *          schema node configurator, or in the case of the root, a factory for
 *          the model
 */
public interface NodeBuilder<T, E> {
  NodeBuilder<T, E> concrete(boolean concrete);

  Optional<Boolean> getConcrete();

  NodeBuilder<T, E> extensible(boolean extensible);

  Optional<Boolean> getExtensible();

  InputInitializerBuilder initializeInput();

  OutputInitializerBuilder initializeOutput();

  default NodeBuilder<T, E> initializeInput(
      Function<? super InputInitializerBuilder, ? extends ValueExpression> initializer) {
    initializeInput().expression(initializer.apply(initializeInput()));
    return this;
  }

  default NodeBuilder<T, E> initializeOutput(
      Function<? super OutputInitializerBuilder, ? extends ValueExpression> initializer) {
    initializeOutput().expression(initializer.apply(initializeOutput()));
    return this;
  }

  ChildBindingPointBuilder<Object, NodeBuilder<T, E>> addChildBindingPoint();

  default NodeBuilder<T, E> addChildBindingPoint(
      Function<ChildBindingPointBuilder<Object, NodeBuilder<T, E>>, ChildBindingPointBuilder<?, NodeBuilder<T, E>>> configuration) {
    configuration.apply(addChildBindingPoint()).endChild();

    return this;
  }

  List<ChildBindingPointBuilder<?, NodeBuilder<T, E>>> getChildBindingPoints();

  E endNode();

  NodeBuilder<T, E> provideValue(TypedObject<?> value);

  Optional<T> getProvidedValue();
}
