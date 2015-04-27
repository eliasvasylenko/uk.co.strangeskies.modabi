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

import java.util.List;

import uk.co.strangeskies.utilities.PropertySet;

public interface AbstractComplexNode<T, S extends AbstractComplexNode<T, S, E>, E extends AbstractComplexNode.Effective<T, S, E>>
		extends BindingNode<T, S, E> {
	interface Effective<T, S extends AbstractComplexNode<T, S, E>, E extends AbstractComplexNode.Effective<T, S, E>>
			extends AbstractComplexNode<T, S, E>, BindingNode.Effective<T, S, E> {
		@Override
		List<Model.Effective<? super T>> baseModel();

		@SuppressWarnings("rawtypes")
		static final PropertySet<AbstractComplexNode.Effective> PROPERTY_SET = new PropertySet<>(
				AbstractComplexNode.Effective.class)
				.add(BindingNode.Effective.PROPERTY_SET)
				.add(AbstractComplexNode.PROPERTY_SET)
				.add(AbstractComplexNode::baseModel);

		@Override
		public default PropertySet<? super E> effectivePropertySet() {
			return PROPERTY_SET;
		}
	}

	@SuppressWarnings("rawtypes")
	static final PropertySet<AbstractComplexNode> PROPERTY_SET = new PropertySet<>(
			AbstractComplexNode.class).add(BindingNode.PROPERTY_SET).add(
			AbstractComplexNode::baseModel);

	@Override
	public default PropertySet<? super S> propertySet() {
		return PROPERTY_SET;
	}

	List<? extends Model<? super T>> baseModel();
}
