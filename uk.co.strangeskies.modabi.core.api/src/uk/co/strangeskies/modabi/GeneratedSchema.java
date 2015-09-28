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
package uk.co.strangeskies.modabi;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.schema.DataBindingType;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.TypeToken;

public interface GeneratedSchema extends Schema {
	default <T> Model<T> generateModel(Class<T> type) {
		return generateModel(TypeToken.over(type));
	}

	default GeneratedSchema generateModels(Class<?>... types) {
		return generateModels(Arrays.stream(types)
				.<TypeToken<?>> map(TypeToken::over).collect(Collectors.toList()));
	}

	<T> Model<T> generateModel(TypeToken<T> type);

	default GeneratedSchema generateModels(TypeToken<?>... types) {
		return generateModels(Arrays.asList(types));
	}

	default GeneratedSchema generateModels(
			Collection<? extends TypeToken<?>> types) {
		for (TypeToken<?> type : types)
			generateModel(type);
		return this;
	}

	default <T> DataBindingType<T> generateDataType(Class<T> type) {
		return generateDataType(TypeToken.over(type));
	}

	default GeneratedSchema generateDataTypes(Class<?>... types) {
		return generateDataTypes(Arrays.stream(types)
				.<TypeToken<?>> map(TypeToken::over).collect(Collectors.toList()));
	}

	<T> DataBindingType<T> generateDataType(TypeToken<T> type);

	default GeneratedSchema generateDataTypes(TypeToken<?>... types) {
		return generateDataTypes(Arrays.asList(types));
	}

	default GeneratedSchema generateDataTypes(
			Collection<? extends TypeToken<?>> types) {
		for (TypeToken<?> type : types)
			generateDataType(type);
		return this;
	}
}
