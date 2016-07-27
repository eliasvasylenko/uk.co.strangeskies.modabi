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

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;

class ModelImpl<T> extends BindingNodeImpl<T, Model<T>> implements Model<T> {
	private final List<Model<? super T>> baseModel;
	private final Schema schema;

	public ModelImpl(ModelConfiguratorImpl<T> configurator) {
		super(configurator);

		LinkedHashSet<Model<? super T>> baseModel = new LinkedHashSet<>();
		configurator.getOverriddenNodes().forEach(n -> baseModel.addAll(n.baseModel()));

		if (configurator.getBaseModel() != null) {
			baseModel.addAll(configurator.getBaseModel().stream().flatMap(m -> m.baseModel().stream()).collect(toList()));
		}

		this.baseModel = Collections.unmodifiableList(new ArrayList<>(baseModel));

		schema = configurator.getSchema();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ModelConfigurator<T> configurator() {
		return (ModelConfigurator<T>) super.configurator();
	}

	@Override
	public final List<Model<? super T>> baseModel() {
		return baseModel;
	}

	@Override
	public Model<T> root() {
		return this;
	}

	@Override
	public Schema schema() {
		return schema;
	}
}
