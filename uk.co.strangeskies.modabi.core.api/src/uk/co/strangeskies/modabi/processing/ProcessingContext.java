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

import uk.co.strangeskies.modabi.Bindings;
import uk.co.strangeskies.modabi.DataTypes;
import uk.co.strangeskies.modabi.Models;
import uk.co.strangeskies.modabi.Provisions;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypedObject;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;

public interface ProcessingContext {
	/**
	 * @return All models registered in the {@link SchemaManager} backing this
	 *         context.
	 */
	Models registeredModels();

	/**
	 * @return All data types registered in the {@link SchemaManager} backing this
	 *         context.
	 */
	DataTypes registeredTypes();

	/**
	 * Get the model of the given name registered in the {@link SchemaManager}
	 * backing this context.
	 * 
	 * @param name
	 *          The name of the model to fetch
	 * @return The model of the given name, or null if no such model exists
	 */
	Model.Effective<?> getModel(QualifiedName name);

	/**
	 * For a given extensible complex node, get a map from possible overriding
	 * models to the nodes resulting from the application of those overrides. The
	 * values of the map are lazily computed, then cached for further use.
	 * 
	 * @param node
	 *          The element to override with a model
	 * @return A mapping from possible overrides to override results
	 */
	<T> ComputingMap<Model<? extends T>, ComplexNode.Effective<? extends T>> getComplexNodeOverrides(ComplexNode<T> node);

	/**
	 * For a given extensible data node, get a map from possible overriding data
	 * types to the nodes resulting from the application of those overrides. The
	 * values of the map are lazily computed, then cached for further use.
	 * 
	 * @param node
	 *          The element to override with a model
	 * @return A mapping from possible overrides to override results
	 */
	<T> ComputingMap<DataType<? extends T>, DataNode.Effective<? extends T>> getDataNodeOverrides(DataNode<T> node);

	/**
	 * @return Objects provided by schema manager for certain types
	 */
	Provisions provisions();

	/**
	 * The stack of schema nodes corresponding to the processing position in a
	 * depth first traversal of a schema node tree. The object at the head of the
	 * stack - the end of the list - is the object currently being processed.
	 * 
	 * @return A list representing the stack, in order from tail to head
	 */
	List<SchemaNode.Effective<?, ?>> getBindingNodeStack();

	/**
	 * @return The node at the head of the {@link #getBindingNodeStack()}.
	 */
	default SchemaNode.Effective<?, ?> getBindingNode() {
		return getBindingNode(0);
	}

	/**
	 * @param parent
	 *          The number of steps back through the stack to reach for a node
	 * @return The node a given number of steps back from the head of the
	 *         {@link #getBindingNodeStack()}
	 */
	default SchemaNode.Effective<?, ?> getBindingNode(int parent) {
		int index = getBindingNodeStack().size() - (1 + parent);
		return index >= 0 ? getBindingNodeStack().get(index) : null;
	}

	/**
	 * Get the stack of typed binding objects corresponding to the processing
	 * position in a depth first traversal of a schema node tree. The object at
	 * the head of the stack - the end of the list - is the object currently being
	 * processed.
	 * 
	 * @return A list representing the stack, in order from tail to head
	 */
	List<TypedObject<?>> getBindingObjectStack();

	/**
	 * @return The object at the head of the {@link #getBindingObjectStack()}.
	 */
	default TypedObject<?> getBindingObject() {
		return getBindingObject(0);
	}

	/**
	 * @param parent
	 *          The number of steps back through the stack to reach for an object
	 * @return The object a given number of steps back from the head of the
	 *         {@link #getBindingObjectStack()}
	 */
	default TypedObject<?> getBindingObject(int parent) {
		int index = getBindingObjectStack().size() - (1 + parent);
		return index >= 0 ? getBindingObjectStack().get(index) : null;
	}

	/**
	 * @return The blocking interface through which a processing thread may signal
	 *         that it is waiting for availability of some dependency or resource
	 */
	BindingBlocker bindingFutureBlocker();

	/**
	 * @return Objects which have been bound so far, or bindings which have been
	 *         encountered so far, during processing
	 */
	Bindings bindings();

	/**
	 * @return The input data source for the processing operation, if applicable
	 */
	Optional<StructuredDataSource> input();

	/**
	 * @return The output data target for the processing operation, if applicable
	 */
	Optional<StructuredDataTarget> output();

	/**
	 * A processing context over some {@link #input()} is exhaustive when
	 * completion of the current {@link #getBindingNode()} will consume the
	 * entirety of any currently processing {@link DataSource}.
	 * 
	 * @return true if any currently binding data is to be exhausted by the
	 *         processing of the current node
	 */
	boolean isExhaustive();

	/**
	 * Convenience method to access a provision over the current processing
	 * context, via {@link Provisions#provide(TypeToken, ProcessingContext)}.
	 * 
	 * @param type
	 *          The type of object to be provided
	 * @return The provided object with respect to this context
	 */
	default <T> TypedObject<T> provide(TypeToken<T> type) {
		return provisions().provide(type, this);
	}

	/**
	 * Convenience method to access a provision over the current processing
	 * context, via {@link Provisions#provide(Class, ProcessingContext)}.
	 * 
	 * @param type
	 *          The type of object to be provided
	 * @return The provided object with respect to this context
	 */
	default <T> TypedObject<T> provide(Class<T> type) {
		return provisions().provide(type, this);
	}

	/**
	 * Convenience method to query a provision over the current processing
	 * context, via {@link Provisions#isProvided(TypeToken, ProcessingContext)}.
	 * 
	 * @param type
	 *          The type of object to be provided
	 * @return True if the object is available, false otherwise
	 */
	default boolean isProvided(TypeToken<?> type) {
		return provisions().isProvided(type, this);
	}

	/**
	 * Convenience method to query a provision over the current processing
	 * context, via {@link Provisions#isProvided(Class, ProcessingContext)}.
	 * 
	 * @param type
	 *          The type of object to be provided
	 * @return True if the object is available, false otherwise
	 */
	default boolean isProvided(Class<?> type) {
		return provisions().isProvided(type, this);
	}
}
