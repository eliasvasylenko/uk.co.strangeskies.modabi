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

import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelBuilder;
import uk.co.strangeskies.modabi.schema.ModelFactory;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface SchemaBuilder {
  Schema create();

  SchemaBuilder qualifiedName(QualifiedName name);

  SchemaBuilder imports(Collection<? extends Class<?>> imports);

  SchemaBuilder dependencies(Collection<? extends Schema> dependencies);

  ModelBuilder addModel();

  default SchemaBuilder addModel(
      QualifiedName name,
      Function<ModelBuilder, ModelFactory<?>> configuration) {
    configuration.apply(addModel().name(name)).createModel();

    return this;
  }

  SchemaBuilder addModel(String name, Function<ModelBuilder, ModelFactory<?>> configuration);

  /*
   * For simple programmatic generation of schemata:
   */

  default <T> Model<T> generateModel(Class<T> type) {
    return generateModel(TypeToken.forClass(type));
  }

  default SchemaBuilder generateModels(Class<?>... types) {
    return generateModels(
        Arrays.stream(types).<TypeToken<?>>map(TypeToken::forClass).collect(Collectors.toList()));
  }

  <T> Model<T> generateModel(TypeToken<T> type);

  default SchemaBuilder generateModels(TypeToken<?>... types) {
    return generateModels(Arrays.asList(types));
  }

  default SchemaBuilder generateModels(Collection<? extends TypeToken<?>> types) {
    for (TypeToken<?> type : types)
      generateModel(type);
    return this;
  }
}
