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

import java.util.List;

import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;
import uk.co.strangeskies.reflection.TypeToken;

public class ModelConfiguratorDecorator<T> extends BindingNodeConfiguratorDecorator<ModelConfigurator<T>, Model<T>, T>
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
	public <V extends T> ModelConfigurator<V> baseModel(List<? extends Model<? super V>> baseModel) {
		setComponent((ModelConfigurator<T>) getComponent().baseModel(baseModel));
		return (ModelConfigurator<V>) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ModelConfigurator<V> dataType(TypeToken<? extends V> bindingClass) {
		return (ModelConfigurator<V>) super.dataType(bindingClass);
	}

	@Override
	public String toString() {
		return getComponent().toString();
	}

	@Override
	public ModelConfigurator<T> copy() {
		return new ModelConfiguratorDecorator<>(getComponent());
	}
}
