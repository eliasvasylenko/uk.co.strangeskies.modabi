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

import static java.util.Arrays.asList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.text.parsing.Parser;

/**
 * The base interface for {@link Schema schema} element nodes. Schemata are made
 * up of a number of {@link ComplexNode models} and {@link SimpleNode data
 * types}, which are themselves a type of schema node, and the root elements of
 * a graph of schema nodes.
 * 
 * @author Elias N Vasylenko
 *
 * @param <S>
 */
public interface Node {
  boolean concrete();

  Parser<?> parser();

  /**
   * @return the set of all <em>direct</em> base nodes, i.e. excluding those which
   *         are transitively implied via other more specific base nodes
   */
  Stream<Node> baseNodes();

  Stream<ChildBindingPoint<?>> children();

  Stream<ChildBindingPoint<?>> descendents(List<QualifiedName> names);

  default Stream<ChildBindingPoint<?>> descendents(QualifiedName... names) {
    return descendents(asList(names));
  }

  ChildBindingPoint<?> child(QualifiedName name);

  Optional<?> providedValue();
}
