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

import static uk.co.strangeskies.modabi.ModabiException.MESSAGES;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.LOOSE_COMPATIBILILTY;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.collection.multimap.MultiHashMap;
import uk.co.strangeskies.collection.multimap.MultiMap;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.token.TypeToken;

public class Models extends NamedSet<QualifiedName, Model<?>> {
  private final MultiMap<QualifiedName, Model<?>, LinkedHashSet<Model<?>>> derivedModels;
  private final MultiMap<Type, Model<?>, LinkedHashSet<Model<?>>> classModels;

  public Models() {
    super(Model::name);
    derivedModels = new MultiHashMap<>(LinkedHashSet::new);
    classModels = new MultiHashMap<>(LinkedHashSet::new);
  }

  @Override
  protected void add(Model<?> element) {
    synchronized (getMutex()) {
      super.add(element);
      mapModel(element);
    }
  }

  private void mapModel(Model<?> model) {
    List<Model<?>> models = new ArrayList<>();
    models.add(model);
    model.baseModel().forEach(models::add);

    derivedModels.addToAll(models.stream().map(Model::name).collect(Collectors.toSet()), model);

    if (model.concrete())
      classModels.add(model.dataType().getType(), model);
  }

  @SuppressWarnings("unchecked")
  public <T> Stream<Model<? extends T>> getDerivedModels(Model<T> model) {
    synchronized (getMutex()) {
      LinkedHashSet<Model<?>> subModelList = derivedModels.get(model.name());

      List<Model<? extends T>> derivedModelList = subModelList == null
          ? new ArrayList<>()
          : subModelList.stream().map(m -> (Model<? extends T>) m).collect(
              Collectors.toCollection(ArrayList::new));

      return derivedModelList.stream();
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> Model<T> cast(Model<?> model, TypeToken<T> dataType) {
    if (model != null
        && !model.dataType().satisfiesConstraintFrom(LOOSE_COMPATIBILILTY, dataType)) {
      throw new ModabiException(MESSAGES.noModelFoundForType(model.name(), dataType.getType()));
    }
    return (Model<T>) model;
  }
}
