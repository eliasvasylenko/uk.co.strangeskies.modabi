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
package uk.co.strangeskies.modabi.impl;

import java.util.Collection;
import java.util.function.Function;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaConfigurator;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;
import uk.co.strangeskies.reflection.token.TypeToken;

public class SchemaConfiguratorDecorator implements SchemaConfigurator {
	private SchemaConfigurator component;

	public SchemaConfiguratorDecorator(SchemaConfigurator component) {
		this.component = component;
	}

	@Override
	public Schema create() {
		return component.create();
	}

	@Override
	public SchemaConfigurator qualifiedName(QualifiedName name) {
		component = component.qualifiedName(name);
		return this;
	}

	@Override
	public SchemaConfigurator imports(Collection<? extends Class<?>> imports) {
		component = component.imports(imports);
		return this;
	}

	@Override
	public SchemaConfigurator dependencies(Collection<? extends Schema> dependencies) {
		component = component.dependencies(dependencies);
		return this;
	}

	@Override
	public ModelConfigurator<?> addModel() {
		return component.addModel();
	}

	@Override
	public <T> Model<T> generateModel(TypeToken<T> type) {
		return component.generateModel(type);
	}

	@Override
	public SchemaConfigurator addModel(String name, Function<ModelConfigurator<?>, ModelConfigurator<?>> configuration) {
		component = component.addModel(name, configuration);
		return this;
	}
}
