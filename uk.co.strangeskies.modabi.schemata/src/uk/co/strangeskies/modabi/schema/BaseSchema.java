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

import java.math.BigDecimal;
import java.math.BigInteger;

import uk.co.strangeskies.mathematics.Interval;
import uk.co.strangeskies.modabi.QualifiedName;

public interface BaseSchema extends Schema {
  QualifiedName BASE_SCHEMA = name("Base");

  QualifiedName ROOT_MODEL = name("root");

  Model<Object> rootModel();

  /*
   * Numerics
   */
  QualifiedName INTEGER_MODEL = name("integer");

  Model<BigInteger> integerModel();

  QualifiedName DECIMAL_MODEL = name("decimal");

  Model<BigDecimal> decimalModel();

  QualifiedName INT_MODEL = name("int");

  Model<Integer> intModel();

  QualifiedName LONG_MODEL = name("long");

  Model<Long> longModel();

  QualifiedName FLOAT_MODEL = name("float");

  Model<Float> floatModel();

  QualifiedName DOUBLE_MODEL = name("double");

  Model<Double> doubleModel();

  QualifiedName BOOLEAN_MODEL = name("boolean");

  Model<Boolean> booleanModel();

  QualifiedName INTERVAL_MODEL = name("interval");

  Model<Interval<Integer>> intervalModel();

  /*
   * Basics
   */
  QualifiedName STRING_MODEL = name("string");

  Model<String> stringModel();

  QualifiedName BINARY_MODEL = name("binary");

  Model<byte[]> binaryModel();

  QualifiedName ENUM_MODEL = name("enum");

  Model<Enum<?>> enumModel();

  private static QualifiedName name(String name) {
    return new QualifiedName(name, MODABI_NAMESPACE);
  }

  static <T extends Enum<?>> T valueOfEnum(Class<T> enumType, String name) {
    @SuppressWarnings("rawtypes")
    Class rawEnumType = enumType;
    @SuppressWarnings("unchecked")
    T result = (T) Enum.valueOf(rawEnumType, name);
    return result;
  }
}
