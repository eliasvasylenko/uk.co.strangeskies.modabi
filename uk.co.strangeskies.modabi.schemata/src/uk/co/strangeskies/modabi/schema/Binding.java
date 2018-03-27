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

import static uk.co.strangeskies.reflection.token.TypedObject.typedObject;

import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypedObject;

public class Binding<T> {
  private final BindingPoint<? super T> node;
  private final Model<? super T> model;
  private final TypeToken<T> dataType;
  private final T data;

  public Binding(
      BindingPoint<? super T> node,
      Model<? super T> model,
      TypeToken<T> dataType,
      T data) {
    this.node = node;
    this.model = model;
    this.dataType = dataType;
    this.data = data;
  }

  public Binding(BindingPoint<? super T> node, Model<? super T> model, TypedObject<T> data) {
    this(node, model, data.getTypeToken(), data.getObject());
  }

  public BindingPoint<? super T> getBindingPoint() {
    return node;
  }

  public Model<? super T> getModel() {
    return model;
  }

  public T getData() {
    return data;
  }

  /*
   * TODO This needs to be given independently of the binding point and model as
   * it may be more specific than both. We need to be able to get the exact type
   * out here, which may have been inferred during the binding process.
   */
  public TypeToken<T> getDataType() {
    return dataType;
  }

  public TypedObject<T> getTypedData() {
    return typedObject(dataType, data);
  }

  @Override
  public String toString() {
    return data + " : " + node.model() + "<" + node.type() + ">";
  }

  // public void updateData();

  // public StructuredDataSource getSource();

  // public void updateSource();
}
