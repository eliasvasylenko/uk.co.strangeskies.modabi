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
package uk.co.strangeskies.modabi.processing;

import java.util.List;
import java.util.Optional;

import uk.co.strangeskies.collection.computingmap.ComputingMap;
import uk.co.strangeskies.modabi.Models;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.io.structured.StructuredDataReader;
import uk.co.strangeskies.modabi.io.structured.StructuredDataWriter;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.Node;
import uk.co.strangeskies.reflection.token.TypedObject;

public interface ProcessingContext {
  /**
   * @return the root manager of the process
   */
  SchemaManager manager();

  /**
   * @return all models registered in the {@link SchemaManager} backing this
   *         context
   */
  Models registeredModels();

  /**
   * @return objects provided by schema manager for certain types
   */
  Provisions provisions();

  /**
   * Get the model of the given name registered in the {@link SchemaManager}
   * backing this context.
   * 
   * @param name
   *          the name of the model to fetch
   * @return the model of the given name, or null if no such model exists
   */
  Model<?> getModel(QualifiedName name);

  /**
   * For a given extensible complex node, get a map from possible overriding
   * models to the nodes resulting from the application of those overrides. The
   * values of the map are lazily computed, then cached for further use.
   * 
   * @param node
   *          the element to override with a model
   * @return a mapping from possible overrides to override results
   */
  ComputingMap<Model<?>, Node<?>> getComplexNodeOverrides(Node<?> node);

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
   * @return the blocking interface through which a processing thread may signal
   *         that it is waiting for availability of some dependency or resource
   */
  BindingBlocker bindingBlocker();

  /**
   * @return objects which have been bound so far and which are visible from this
   *         processing context
   */
  ProcessedBindings bindings();

  /**
   * @return the input data source for the processing operation, if applicable
   */
  Optional<StructuredDataReader> input();

  /**
   * @return the output data target for the processing operation, if applicable
   */
  Optional<StructuredDataWriter> output();
}
