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

import static uk.co.strangeskies.reflection.token.TypedObject.typedObject;
import static uk.co.strangeskies.utilities.collection.StreamUtilities.throwingMerger;

import java.util.Optional;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.reflection.token.TypedObject;

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
public interface SchemaNode<T> {
	enum Format {
		PROPERTY, CONTENT, SIMPLE, COMPLEX
	}

	boolean concrete();

	boolean extensible();

	/**
	 * @return the set of all <em>direct</em> base models, i.e. excluding those
	 *         which are transitively implied via other more specific base models
	 */
	Stream<Model<?>> baseModel();

	/**
	 * Get the schema node configurator which created this schema node, or in the
	 * case of a mutable configurator implementation, a copy thereof.
	 * 
	 * @return the creating configurator
	 */
	SchemaNodeConfigurator<T, ?> configurator();

	/**
	 * @return the set of all <em>direct</em> base nodes, i.e. excluding those
	 *         which are transitively implied via other more specific base nodes
	 */
	Stream<SchemaNode<?>> baseNodes();

	BindingPoint<T> bindingPoint();

	Stream<ChildBindingPoint<?>> childBindingPoints();

	ValueResolution providedValuesResolution();

	DataSource providedValuesBuffer();

	Stream<T> providedValues();

	default Stream<TypedObject<T>> typedProvidedValues() {
		return providedValues().map(v -> typedObject(bindingPoint().dataType(), v));
	}

	default Optional<T> providedValue() {
		return providedValues().reduce(throwingMerger());
	}

	default Optional<TypedObject<T>> typedProvidedValue() {
		return providedValue().map(v -> typedObject(bindingPoint().dataType(), v));
	}

	default boolean isValueProvided() {
		return providedValues() != null;
	}
}
