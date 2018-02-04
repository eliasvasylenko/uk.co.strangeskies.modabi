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

import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.reflection.token.TypedObject;

/**
 * 
 * @author Elias N Vasylenko
 *
 * @param <E>
 *          the end product of the builder, which will either be the parent
 *          schema node builder, or in the case of the root, a factory for the
 *          model
 */
public interface NodeBuilder<E> {
  NodeBuilder<E> concrete(boolean concrete);

  Optional<Boolean> getConcrete();

  NodeBuilder<E> inputInitialization(Expression expression);

  Expression getInputInitialization();

  NodeBuilder<E> outputInitialization(Expression expression);

  Expression getOutputInitialization();

  ChildBindingPointBuilder<NodeBuilder<E>> addChildBindingPoint();

  default NodeBuilder<E> addChildBindingPoint(
      Function<ChildBindingPointBuilder<NodeBuilder<E>>, ChildBindingPointBuilder<NodeBuilder<E>>> configuration) {
    return configuration.apply(addChildBindingPoint()).endChild();
  }

  List<? extends ChildBindingPointBuilder<?>> getChildBindingPoints();

  E endNode();

  NodeBuilder<E> provideValue(TypedObject<?> value);

  Optional<?> getProvidedValue();
}
