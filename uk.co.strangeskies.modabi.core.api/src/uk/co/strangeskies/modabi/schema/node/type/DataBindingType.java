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
package uk.co.strangeskies.modabi.schema.node.type;

import uk.co.strangeskies.modabi.schema.node.BindingNode;
import uk.co.strangeskies.utilities.PropertySet;

public interface DataBindingType<T> extends
		BindingNode<T, DataBindingType<T>, DataBindingType.Effective<T>> {
	interface Effective<T> extends DataBindingType<T>,
			BindingNode.Effective<T, DataBindingType<T>, Effective<T>> {
		@SuppressWarnings("rawtypes")
		static final PropertySet<DataBindingType.Effective> PROPERTY_SET = new PropertySet<>(
				DataBindingType.Effective.class)
				.add(BindingNode.Effective.PROPERTY_SET)
				.add(DataBindingType.PROPERTY_SET).add(DataBindingType::isPrivate)
				.add(DataBindingType::baseType);

		@SuppressWarnings("unchecked")
		@Override
		default PropertySet<DataBindingType.Effective<T>> effectivePropertySet() {
			return (PropertySet<DataBindingType.Effective<T>>) (Object) PROPERTY_SET;
		}
	}

	Boolean isPrivate();

	DataBindingType<? super T> baseType();

	@SuppressWarnings("rawtypes")
	static final PropertySet<DataBindingType> PROPERTY_SET = new PropertySet<>(
			DataBindingType.class).add(BindingNode.PROPERTY_SET)
			.add(DataBindingType::isPrivate).add(DataBindingType::baseType);

	@SuppressWarnings("unchecked")
	@Override
	default PropertySet<DataBindingType<T>> propertySet() {
		return (PropertySet<DataBindingType<T>>) (Object) PROPERTY_SET;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	default Class<Effective<T>> getEffectiveClass() {
		return (Class) Effective.class;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	default Class<DataBindingType<T>> getNodeClass() {
		return (Class) DataBindingType.class;
	}
}
