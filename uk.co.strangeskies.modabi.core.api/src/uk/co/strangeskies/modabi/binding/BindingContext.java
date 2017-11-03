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
package uk.co.strangeskies.modabi.binding;

import java.util.List;
import java.util.Optional;

import uk.co.strangeskies.modabi.Bindings;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.io.structured.StructuredDataReader;
import uk.co.strangeskies.modabi.io.structured.StructuredDataWriter;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.reflection.token.TypedObject;

public interface BindingContext {
  /**
   * @return the root manager of the process
   */
  SchemaManager manager();

  /**
   * @return objects provided by schema manager for certain types
   */
  Provisions provisions();

  /**
   * The stack of schema nodes corresponding to the processing position in a depth
   * first traversal of a schema node tree. The object at the head of the stack -
   * the end of the list - is the object currently being processed.
   * 
   * @return a list representing the stack, in order from tail to head
   */
  List<BindingPoint<?>> getBindingNodeStack();

  /**
   * @return the node at the head of the {@link #getBindingNodeStack()}.
   */
  default BindingPoint<?> getBindingPoint() {
    return getBindingPoint(0);
  }

  /**
   * @param parent
   *          the number of steps back through the stack to reach for a node
   * @return the node a given number of steps back from the head of the
   *         {@link #getBindingNodeStack()}
   */
  default BindingPoint<?> getBindingPoint(int parent) {
    int index = getBindingNodeStack().size() - (1 + parent);
    return index >= 0 ? getBindingNodeStack().get(index) : null;
  }

  /**
   * Get the stack of typed binding objects corresponding to the processing
   * position in a depth first traversal of a schema node tree. The object at the
   * head of the stack - the end of the list - is the object currently being
   * processed.
   * 
   * @return a list representing the stack, in order from tail to head
   */
  List<TypedObject<?>> getBindingObjectStack();

  /**
   * @return the object at the head of the {@link #getBindingObjectStack()}.
   */
  default TypedObject<?> getBindingObject() {
    return getBindingObject(0);
  }

  /**
   * @param parent
   *          the number of steps back through the stack to reach for an object
   * @return the object a given number of steps back from the head of the
   *         {@link #getBindingObjectStack()}
   */
  default TypedObject<?> getBindingObject(int parent) {
    int index = getBindingObjectStack().size() - (1 + parent);
    return index >= 0 ? getBindingObjectStack().get(index) : null;
  }

  /**
   * @return objects which have been bound so far during processing
   */
  Bindings localBindings();

  /**
   * @return objects which have been bound so far in the set provided to this
   *         processing context
   */
  Bindings globalBindings();

  /**
   * @return the input data source for the processing operation, if applicable
   */
  Optional<StructuredDataReader> input();

  /**
   * @return the output data target for the processing operation, if applicable
   */
  Optional<StructuredDataWriter> output();
}
