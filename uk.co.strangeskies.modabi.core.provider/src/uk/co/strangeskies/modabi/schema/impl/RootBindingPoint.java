/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.provider.
 *
 * uk.co.strangeskies.modabi.core.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.schema.impl;

import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.token.TypeToken;

public class RootBindingPoint<T> implements BindingPoint<T> {
  private final TypeToken<T> dataType;
  private final Model<? super T> model;

  public RootBindingPoint(TypeToken<T> dataType, Model<? super T> model) {
    this.dataType = dataType;
    this.model = model;
  }

  public RootBindingPoint(Model<T> model) {
    this.dataType = model.dataType();
    this.model = model;
  }

  @Override
  public TypeToken<T> dataType() {
    return dataType;
  }

  @Override
  public Model<? super T> model() {
    return model;
  }
}
