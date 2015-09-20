/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.provider.
 *
 * uk.co.strangeskies.modabi.core.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.impl.schema;

import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.DataBindingType;
import uk.co.strangeskies.utilities.PropertySet;

public class DataBindingTypeImpl<T> extends
		BindingNodeImpl<T, DataBindingType<T>, DataBindingType.Effective<T>>
		implements DataBindingType<T> {
	private static class Effective<T>
			extends
			BindingNodeImpl.Effective<T, DataBindingType<T>, DataBindingType.Effective<T>>
			implements DataBindingType.Effective<T> {
		private final Boolean isPrivate;

		private final DataBindingType.Effective<? super T> baseType;

		public Effective(
				OverrideMerge<DataBindingType<T>, DataBindingTypeConfiguratorImpl<T>> overrideMerge) {
			super(overrideMerge);

			isPrivate = overrideMerge.node().isPrivate() != null
					&& overrideMerge.node().isPrivate();

			baseType = overrideMerge.configurator().getBaseType() == null ? null
					: overrideMerge.configurator().getBaseType().effective();
		}

		@Override
		public Boolean isPrivate() {
			return isPrivate;
		}

		@Override
		public DataBindingType.Effective<? super T> baseType() {
			return baseType;
		}

		@SuppressWarnings("rawtypes")
		protected static final PropertySet<DataBindingType.Effective> PROPERTY_SET = new PropertySet<>(
				DataBindingType.Effective.class)
				.add(BindingNodeImpl.Effective.PROPERTY_SET)
				.add(DataBindingTypeImpl.PROPERTY_SET).add(DataBindingType::isPrivate)
				.add(DataBindingType::baseType);

		@SuppressWarnings("unchecked")
		@Override
		protected PropertySet<DataBindingType.Effective<T>> effectivePropertySet() {
			return (PropertySet<DataBindingType.Effective<T>>) (Object) PROPERTY_SET;
		}
	}

	private final DataBindingTypeImpl.Effective<T> effective;

	private final Boolean isPrivate;

	private final DataBindingType<? super T> baseType;

	public DataBindingTypeImpl(DataBindingTypeConfiguratorImpl<T> configurator) {
		super(configurator);

		isPrivate = configurator.getIsPrivate();

		baseType = configurator.getBaseType();

		effective = new DataBindingTypeImpl.Effective<>(
				DataBindingTypeConfiguratorImpl.overrideMerge(this, configurator));
	}

	@SuppressWarnings("rawtypes")
	protected static final PropertySet<DataBindingType> PROPERTY_SET = new PropertySet<>(
			DataBindingType.class).add(BindingNodeImpl.PROPERTY_SET)
			.add(DataBindingType::isPrivate).add(DataBindingType::baseType);

	@SuppressWarnings("unchecked")
	@Override
	protected PropertySet<DataBindingType<T>> propertySet() {
		return (PropertySet<DataBindingType<T>>) (Object) PROPERTY_SET;
	}

	@Override
	public Boolean isPrivate() {
		return isPrivate;
	}

	@Override
	public DataBindingType<? super T> baseType() {
		return baseType;
	}

	@Override
	public DataBindingTypeImpl.Effective<T> effective() {
		return effective;
	}
}
