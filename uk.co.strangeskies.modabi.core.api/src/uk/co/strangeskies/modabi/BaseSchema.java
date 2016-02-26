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

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.Primitive;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.Enumeration;

public interface BaseSchema extends Schema {
	public static final QualifiedName QUALIFIED_NAME = new QualifiedName(BaseSchema.class.getSimpleName(),
			MODABI_NAMESPACE);

	public interface DerivedTypes {
		DataType<Object[]> arrayType();

		DataType<Collection<?>> collectionType();

		DataType<List<?>> listType();

		DataType<Set<?>> setType();

		DataType<URI> uriType();

		DataType<URL> urlType();

		DataType<Object> referenceType();

		DataType<DataSource> bufferedDataType();

		DataType<Range<Integer>> rangeType();

		DataType<Enum<?>> enumType();

		DataType<Enumeration<?>> enumerationType();

		DataType<Class<?>> classType();

		DataType<Type> typeType();

		DataType<TypeToken<?>> typeTokenType();

		DataType<AnnotatedType> annotatedTypeType();

		/*
		 * during binding / unbinding magically adds items to bindings list (so can
		 * be referenced)
		 */
		DataType<Collection<?>> includeType();

		/*
		 * retrieves objects already bound by SchemaBinder and 'includes' them, or
		 * some children of them. Blocks if we are waiting for them.
		 */
		DataType<Object> importType();
	}

	public interface BaseModels {
		Model<?> simpleModel();

		Model<Map<?, ?>> mapModel();
	}

	<T> DataType<T> primitiveType(Primitive<T> type);

	DerivedTypes derivedTypes();

	BaseModels models();
}
