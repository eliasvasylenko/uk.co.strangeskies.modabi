/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.modabi.schema.node;

import uk.co.strangeskies.modabi.schema.management.SchemaProcessingContext;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.PropertySet;

public interface ChildNode<S extends ChildNode<S, E>, E extends ChildNode.Effective<S, E>>
		extends SchemaNode<S, E> {
	interface Effective<S extends ChildNode<S, E>, E extends Effective<S, E>>
			extends ChildNode<S, E>, SchemaNode.Effective<S, E> {
		TypeToken<?> getPreInputType();

		void process(SchemaProcessingContext context);

		@SuppressWarnings("rawtypes")
		static final PropertySet<ChildNode.Effective> PROPERTY_SET = new PropertySet<>(
				ChildNode.Effective.class).add(ChildNode.PROPERTY_SET)
				.add(SchemaNode.Effective.PROPERTY_SET)
				.add(ChildNode.Effective::getPreInputType);

		@Override
		default PropertySet<? super E> effectivePropertySet() {
			return PROPERTY_SET;
		}
	}

	@SuppressWarnings("rawtypes")
	static final PropertySet<ChildNode> PROPERTY_SET = new PropertySet<>(
			ChildNode.class).add(SchemaNode.PROPERTY_SET).add(
			ChildNode::getPostInputType);

	@Override
	public default PropertySet<? super S> propertySet() {
		return PROPERTY_SET;
	}

	TypeToken<?> getPostInputType();
}
