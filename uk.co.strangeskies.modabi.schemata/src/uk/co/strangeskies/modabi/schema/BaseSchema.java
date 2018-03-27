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
package uk.co.strangeskies.modabi.schema;

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
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.utility.Enumeration;

public interface BaseSchema extends Schema {
  public static final QualifiedName BASE_SCHEMA = new QualifiedName(
      BaseSchema.class.getSimpleName(),
      MODABI_NAMESPACE);

  QualifiedName ROOT_MODEL = name("root");

  Model<Object> rootModel();

  /*
   * Primitives
   */
  QualifiedName INTEGER_MODEL = name("integer");
  QualifiedName DECIMAL_MODEL = name("decimal");
  QualifiedName INT_MODEL = name("int");
  QualifiedName LONG_MODEL = name("long");
  QualifiedName FLOAT_MODEL = name("float");
  QualifiedName DOUBLE_MODEL = name("double");
  QualifiedName BOOLEAN_MODEL = name("boolean");

  Model<BigInteger> integerModel();

  Model<BigDecimal> decimalModel();

  Model<Integer> intModel();

  Model<Long> longModel();

  Model<Float> floatModel();

  Model<Double> doubleModel();

  Model<Boolean> booleanModel();

  /*
   * Basics
   */
  QualifiedName STRING_MODEL = name("string");
  QualifiedName BINARY_MODEL = name("binary");
  QualifiedName ENUM_MODEL = name("enum");
  QualifiedName ENUMERATION_MODEL = name("enumeration");

  Model<String> stringModel();

  Model<byte[]> binaryModel();

  Model<Enum<?>> enumModel();

  Model<Enumeration<?>> enumerationModel();

  /*
   * Collections
   */
  QualifiedName ARRAY_MODEL = name("array");
  QualifiedName COLLECTION_MODEL = name("collection");
  QualifiedName LIST_MODEL = name("list");
  QualifiedName SET_MODEL = name("set");
  QualifiedName MAP_MODEL = name("map");

  Model<Object[]> arrayModel();

  Model<Collection<?>> collectionModel();

  Model<List<?>> listModel();

  Model<Set<?>> setModel();

  Model<Map<?, ?>> mapModel();

  /*
   * External library
   */
  QualifiedName URI_MODEL = name("uri");
  QualifiedName URL_MODEL = name("url");

  Model<URI> uriModel();

  Model<URL> urlModel();

  /*
   * Internal library
   */
  QualifiedName QUALIFIED_NAME_MODEL = name("qualifiedName");
  QualifiedName INTERVAL_MODEL = name("interval");

  Model<QualifiedName> qualifiedNameModel();

  Model<Interval<Integer>> intervalModel();

  /*
   * Reflection
   */
  QualifiedName PACKAGE_MODEL = name("package");
  QualifiedName CLASS_MODEL = name("class");
  QualifiedName TYPE_MODEL = name("type");
  QualifiedName ANNOTATED_TYPE_MODEL = name("annotatedType");
  QualifiedName TYPE_TOKEN_MODEL = name("typeToken");

  Model<Package> packageModel();

  Model<Class<?>> classModel();

  Model<Type> typeModel();

  Model<AnnotatedType> annotatedTypeModel();

  Model<TypeToken<?>> typeTokenModel();

  // TODO iirc Java 9 will allow this to be static
  @Deprecated
  static QualifiedName name(String name) {
    return new QualifiedName(name, BASE_SCHEMA.getNamespace());
  }
}
