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

import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.co.strangeskies.modabi.QualifiedName;

public interface CollectionsSchema extends Schema {
  QualifiedName COLLECTIONS_SCHEMA = name("Collections");

  QualifiedName LIST_MODEL = name("list");

  Model<List<?>> listModel();

  QualifiedName SET_MODEL = name("set");

  // TODO the ? extends seems to be required as an eclipse bug?
  Model<? extends Set<?>> setModel();

  QualifiedName MAP_MODEL = name("map");

  Model<Map<?, ?>> mapModel();

  private static QualifiedName name(String name) {
    return new QualifiedName(name, MODABI_NAMESPACE);
  }
}
