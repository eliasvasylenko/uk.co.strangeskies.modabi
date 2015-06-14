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
package uk.co.strangeskies.modabi.schema;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.DataType;
import uk.co.strangeskies.modabi.schema.node.DataBindingType;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.Enumeration;

public interface BaseSchema extends Schema {
	public interface DerivedTypes {
		DataBindingType<Object[]> arrayType();

		DataBindingType<Collection<?>> collectionType();

		DataBindingType<List<?>> listType();

		DataBindingType<Set<?>> setType();

		DataBindingType<Object> referenceType();

		DataBindingType<DataSource> bufferedDataType();

		DataBindingType<Range<Integer>> rangeType();

		DataBindingType<Enum<?>> enumType();

		DataBindingType<Enumeration<?>> enumerationType();

		DataBindingType<Class<?>> classType();

		DataBindingType<Type> typeType();

		DataBindingType<TypeToken<?>> typeTokenType();

		DataBindingType<AnnotatedType> annotatedTypeType();

		/*
		 * during binding / unbinding magically adds items to bindings list (so can
		 * be referenced)
		 */
		DataBindingType<Collection<?>> includeType();

		/*
		 * retrieves objects already bound by SchemaBinder and 'includes' them, or
		 * some children of them. Blocks if we are waiting for them.
		 */
		DataBindingType<Object> importType();
	}

	public interface BaseModels {}

	<T> DataBindingType<T> primitiveType(DataType<T> type);

	DerivedTypes derivedTypes();

	BaseModels models();
}
