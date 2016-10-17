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

import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.utilities.Self;

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
public interface SchemaNode extends Self<SchemaNode> {
	enum Format {
		PROPERTY, CONTENT, SIMPLE, COMPLEX
	}

	/**
	 * Get the schema node configurator which created this schema node, or in the
	 * case of a mutable configurator implementation, a copy thereof.
	 * 
	 * @return the creating configurator
	 */
	SchemaNodeConfigurator configurator();

	/**
	 * @return the set of all <em>direct</em> base nodes, i.e. excluding those
	 *         which are transitively included via other more specific base nodes
	 */
	List<SchemaNode> baseNodes();

	BindingPoint<?> parentBindingPoint();

	Schema schema();

	List<ChildBindingPoint<?>> childBindingPoints();

	@Override
	default SchemaNode copy() {
		return getThis();
	}
}
