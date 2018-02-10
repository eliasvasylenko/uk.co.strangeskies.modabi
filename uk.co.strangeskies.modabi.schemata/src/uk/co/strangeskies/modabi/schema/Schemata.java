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

import uk.co.strangeskies.modabi.NamedSet;
import uk.co.strangeskies.modabi.QualifiedName;

public class Schemata extends NamedSet<QualifiedName, Schema> {
  private final BaseSchema baseSchema;
  private final Models models;

  public Schemata(BaseSchema baseSchema) {
    super(Schema::name);
    this.baseSchema = baseSchema;
    this.models = new Models();
  }

  @Override
  public void add(Schema element) {
    /*
     * TODO if the set already contains any of the given schema names (inc. deps.)
     * and they are not identity equivalent, then fail without changing the set.
     * 
     * TODO if the set of models already contains any of the model names provided by
     * the new schemata or its dependencies, then fail without changing the set.
     * 
     * TODO add all the dependencies of the schemata
     */
    super.add(element);
  }

  public Models models() {
    return models;
  }

  public BaseSchema getBaseSchema() {
    return baseSchema;
  }
}
