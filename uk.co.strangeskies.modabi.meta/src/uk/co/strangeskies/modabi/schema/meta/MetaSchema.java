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
package uk.co.strangeskies.modabi.schema.meta;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.Schema;

public interface MetaSchema extends Schema {
  QualifiedName META_SCHEMA = new QualifiedName(MetaSchema.class.getSimpleName(), MODABI_NAMESPACE);

  QualifiedName BINDING_CONDITION_MODEL = name("bindingCondition");
  QualifiedName AND_CONDITION_MODEL = name("andCondition");
  QualifiedName OR_CONDITION_MODEL = name("orCondition");

  QualifiedName SCHEMA_MODEL = name("schema");
  QualifiedName SCHEMA_BUILDER_MODEL = name("schemaBuilder");
  QualifiedName MODEL_BUILDER_MODEL = name("modelBuilder");
  QualifiedName CHILD_BUILDER_MODEL = name("childBuilder");
  QualifiedName CHILD_SEQUENCE_BUILDER_MODEL = name("childSequenceBuilder");

  Model<Schema> getSchemaModel();

  Model<SchemaBuilder> getSchemaBuilderModel();

  // TODO iirc Java 9 will allow this to be static
  @Deprecated
  static QualifiedName name(String name) {
    return new QualifiedName(name, META_SCHEMA.getNamespace());
  }
}
