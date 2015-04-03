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
import uk.co.strangeskies.utilities.PropertySet;

public interface ComplexNode<T> extends
		AbstractComplexNode<T, ComplexNode<T>, ComplexNode.Effective<T>>,
		BindingChildNode<T, ComplexNode<T>, ComplexNode.Effective<T>> {
	interface Effective<T> extends ComplexNode<T>,
			AbstractComplexNode.Effective<T, ComplexNode<T>, Effective<T>>,
			BindingChildNode.Effective<T, ComplexNode<T>, Effective<T>> {
		@Override
		default void process(SchemaProcessingContext context) {
			context.accept(this);
		}

		@SuppressWarnings("rawtypes")
		static final PropertySet<ComplexNode.Effective> PROPERTY_SET = new PropertySet<>(
				ComplexNode.Effective.class).add(ComplexNode.PROPERTY_SET).add(
				BindingChildNode.Effective.PROPERTY_SET);

		@SuppressWarnings("unchecked")
		@Override
		public default PropertySet<ComplexNode.Effective<T>> effectivePropertySet() {
			return (PropertySet<ComplexNode.Effective<T>>) (Object) PROPERTY_SET;
		}
	}

	Boolean isInline();

	@SuppressWarnings("rawtypes")
	static final PropertySet<ComplexNode> PROPERTY_SET = new PropertySet<>(
			ComplexNode.class).add(BindingChildNode.PROPERTY_SET).add(
			AbstractComplexNode::baseModel);

	@SuppressWarnings("unchecked")
	@Override
	public default PropertySet<ComplexNode<T>> propertySet() {
		return (PropertySet<ComplexNode<T>>) (Object) PROPERTY_SET;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	default Class<Effective<T>> getEffectiveClass() {
		return (Class) Effective.class;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	default Class<ComplexNode<T>> getNodeClass() {
		return (Class) ComplexNode.class;
	}
}
