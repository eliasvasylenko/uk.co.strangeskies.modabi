/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.modabi;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.DataTypeConfigurator;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.factory.Factory;

public interface SchemaConfigurator extends Factory<Schema> {
	SchemaConfigurator qualifiedName(QualifiedName name);

	SchemaConfigurator imports(Collection<? extends Class<?>> imports);

	SchemaConfigurator dependencies(Collection<? extends Schema> dependencies);

	DataTypeConfigurator<Object> addDataType();

	default SchemaConfigurator addDataType(QualifiedName name,
			Function<DataTypeConfigurator<Object>, DataTypeConfigurator<?>> configuration) {
		configuration.apply(addDataType().name(name)).create();

		return this;
	}

	SchemaConfigurator addDataType(String name,
			Function<DataTypeConfigurator<Object>, DataTypeConfigurator<?>> configuration);

	ModelConfigurator<Object> addModel();

	default SchemaConfigurator addModel(QualifiedName name,
			Function<ModelConfigurator<Object>, ModelConfigurator<?>> configuration) {
		configuration.apply(addModel().name(name)).create();

		return this;
	}

	SchemaConfigurator addModel(String name, Function<ModelConfigurator<Object>, ModelConfigurator<?>> configuration);

	/*
	 * For simple programmatic generation of schemata:
	 */

	default <T> Model<T> generateModel(Class<T> type) {
		return generateModel(TypeToken.over(type));
	}

	default SchemaConfigurator generateModels(Class<?>... types) {
		return generateModels(Arrays.stream(types).<TypeToken<?>> map(TypeToken::over).collect(Collectors.toList()));
	}

	<T> Model<T> generateModel(TypeToken<T> type);

	default SchemaConfigurator generateModels(TypeToken<?>... types) {
		return generateModels(Arrays.asList(types));
	}

	default SchemaConfigurator generateModels(Collection<? extends TypeToken<?>> types) {
		for (TypeToken<?> type : types)
			generateModel(type);
		return this;
	}

	default <T> DataType<T> generateDataType(Class<T> type) {
		return generateDataType(TypeToken.over(type));
	}

	default SchemaConfigurator generateDataTypes(Class<?>... types) {
		return generateDataTypes(Arrays.stream(types).<TypeToken<?>> map(TypeToken::over).collect(Collectors.toList()));
	}

	<T> DataType<T> generateDataType(TypeToken<T> type);

	default SchemaConfigurator generateDataTypes(TypeToken<?>... types) {
		return generateDataTypes(Arrays.asList(types));
	}

	default SchemaConfigurator generateDataTypes(Collection<? extends TypeToken<?>> types) {
		for (TypeToken<?> type : types)
			generateDataType(type);
		return this;
	}
}
