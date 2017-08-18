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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.co.strangeskies.mathematics.Interval;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.utility.Enumeration;

public interface BaseSchema extends Schema {
  public static final QualifiedName QUALIFIED_NAME = new QualifiedName(
      BaseSchema.class.getSimpleName(),
      MODABI_NAMESPACE);

  Model<String> stringModel();

  Model<Object> rootModel();

  Model<byte[]> binaryModel();

  Model<BigInteger> integerModel();

  Model<BigDecimal> decimalModel();

  Model<Integer> intModel();

  Model<Long> longModel();

  Model<Float> floatModel();

  Model<Double> doubleModel();

  Model<Boolean> booleanModel();

  Model<QualifiedName> qualifiedNameModel();

  Model<Object[]> arrayModel();

  Model<Collection<?>> collectionModel();

  Model<List<?>> listModel();

  Model<Set<?>> setModel();

  Model<URI> uriModel();

  Model<URL> urlModel();

  Model<?> referenceModel();

  Model<?> bindingReferenceModel();

  Model<Interval<Integer>> rangeModel();

  Model<Enum<?>> enumModel();

  Model<Enumeration<?>> enumerationModel();

  Model<Package> packageModel();

  Model<Class<?>> classModel();

  Model<Type> typeModel();

  Model<TypeToken<?>> typeTokenModel();

  Model<AnnotatedType> annotatedTypeModel();

  Model<Map<?, ?>> mapModel();

  /*
   * during binding / unbinding magically adds items to bindings list (so can be
   * referenced)
   */
  Model<Collection<?>> includeModel();

  /*
   * retrieves objects already bound by SchemaBinder and 'includes' them, or some
   * children of them. Blocks if we are waiting for them.
   */
  Model<Object> importModel();
}
