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

	/*
	 * Access to provided objects
	 */
	Provisions provisions();

	/*
	 * Access to position in binding node tree traversal
	 */
	List<SchemaNode.Effective<?, ?>> bindingNodeStack();

	default SchemaNode.Effective<?, ?> bindingNode() {
		return bindingNode(0);
	}

	default SchemaNode.Effective<?, ?> bindingNode(int parent) {
		int index = bindingNodeStack().size() - (1 + parent);
		return index >= 0 ? bindingNodeStack().get(index) : null;
	}

	/*
	 * Access to object being bound or unbound
	 */
	List<TypedObject<?>> bindingObjectStack();

	default TypedObject<?> bindingObject() {
		return bindingObject(0);
	}

	default TypedObject<?> bindingObject(int parent) {
		int index = bindingObjectStack().size() - (1 + parent);
		return index >= 0 ? bindingObjectStack().get(index) : null;
	}

	/*
	 * Access to bindings encountered so far
	 */
	Bindings bindings();

	/*
	 * Input and or output, where available
	 */
	Optional<StructuredDataSource> input();

	Optional<StructuredDataTarget> output();

	boolean isExhaustive();

	default <T> TypedObject<T> provide(TypeToken<T> type) {
		return provisions().provide(type, this);
	}

	default <T> TypedObject<T> provide(Class<T> type) {
		return provisions().provide(type, this);
	}

	default boolean isProvided(TypeToken<?> type) {
		return provisions().isProvided(type, this);
	}

	default boolean isProvided(Class<?> type) {
		return provisions().isProvided(type, this);
	}

	Models registeredModels();

	DataTypes registeredTypes();
}
