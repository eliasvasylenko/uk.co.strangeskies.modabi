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
package uk.co.strangeskies.modabi.schema.types;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface TypesSchema extends Schema {
  QualifiedName REFLECTION_SCHEMA = name("Reflection");

  QualifiedName PACKAGE_MODEL = name("package");

  Model<Package> packageModel();

  QualifiedName CLASS_MODEL = name("class");

  Model<Class<?>> classModel();

  QualifiedName TYPE_MODEL = name("type");

  Model<Type> typeModel();

  QualifiedName ANNOTATED_TYPE_MODEL = name("annotatedType");

  Model<AnnotatedType> annotatedTypeModel();

  QualifiedName TYPE_TOKEN_MODEL = name("typeToken");

  Model<TypeToken<?>> typeTokenModel();

  private static QualifiedName name(String name) {
    return new QualifiedName(name, MODABI_NAMESPACE);
  }
}
