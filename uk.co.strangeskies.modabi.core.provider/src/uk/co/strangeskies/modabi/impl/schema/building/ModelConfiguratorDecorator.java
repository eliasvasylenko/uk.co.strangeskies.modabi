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
package uk.co.strangeskies.modabi.impl.schema.building;

import java.util.Collection;

import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;
import uk.co.strangeskies.reflection.TypeToken;

public class ModelConfiguratorDecorator<T> extends BindingPointConfiguratorDecorator<T, ModelConfigurator<T>>
		implements ModelConfigurator<T> {
	public ModelConfiguratorDecorator(ModelConfigurator<T> component) {
		super(component);
	}

	@Override
	public Model<T> create() {
		return getComponent().create();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> ModelConfigurator<V> dataType(TypeToken<? extends V> dataType) {
		return (ModelConfigurator<V>) super.dataType(dataType);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> ModelConfigurator<V> baseModel(Collection<? extends Model<? extends V>> baseModel) {
		return (ModelConfigurator<V>) super.baseModel(baseModel);
	}

	@Override
	public ModelConfiguratorDecorator<T> copy() {
		return new ModelConfiguratorDecorator<>(getComponent().copy());
	}
}
