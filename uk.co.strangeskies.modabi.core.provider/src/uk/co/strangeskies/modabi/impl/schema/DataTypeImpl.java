/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
import uk.co.strangeskies.modabi.schema.DataType;

public class DataTypeImpl<T>
		extends BindingNodeImpl<T, DataType<T>, DataType.Effective<T>>
		implements DataType<T> {
	private static class Effective<T>
			extends BindingNodeImpl.Effective<T, DataType<T>, DataType.Effective<T>>
			implements DataType.Effective<T> {
		private final Boolean isPrivate;

		private final DataType.Effective<? super T> baseType;

		public Effective(
				OverrideMerge<DataType<T>, DataTypeConfiguratorImpl<T>> overrideMerge) {
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
		public DataType.Effective<? super T> baseType() {
			return baseType;
		}
	}

	private final DataTypeImpl.Effective<T> effective;

	private final Boolean isPrivate;

	private final DataType<? super T> baseType;

	public DataTypeImpl(DataTypeConfiguratorImpl<T> configurator) {
		super(configurator);

		isPrivate = configurator.getIsPrivate();

		baseType = configurator.getBaseType();

		effective = new DataTypeImpl.Effective<>(
				DataTypeConfiguratorImpl.overrideMerge(this, configurator));
	}

	@Override
	public Boolean isPrivate() {
		return isPrivate;
	}

	@Override
	public DataType<? super T> baseType() {
		return baseType;
	}

	@Override
	public DataTypeImpl.Effective<T> effective() {
		return effective;
	}
}
