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

import java.util.stream.Stream;

import uk.co.strangeskies.reflection.token.ReifiedToken;

public interface SchemaManager {
  BaseSchema getBaseSchema();

  MetaSchema getMetaSchema();

  Stream<Provider> getProviders();

  Schemata registeredSchemata();

  Models registeredModels();

  DataFormats registeredFormats();

  InputBinder<?> bindInput();

  default InputBinder<Schema> bindSchema() {
    return bindInput().to(getMetaSchema().getSchemaModel());
  }

  <T> OutputBinder<? super T> bindOutput(T data);

  default <T extends ReifiedToken<T>> OutputBinder<? super T> bindOutput(T data) {
    return bindOutput(data).from(data.getThisTypeToken());
  }
}
