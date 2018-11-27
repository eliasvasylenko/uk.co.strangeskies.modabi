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

import static uk.co.strangeskies.mathematics.Interval.leftBounded;
import static uk.co.strangeskies.modabi.expression.Expressions.invokeConstructor;
import static uk.co.strangeskies.modabi.schema.BindingConstraint.occurrences;
import static uk.co.strangeskies.modabi.schema.meta.BindingExpressions.boundValue;
import static uk.co.strangeskies.modabi.schema.meta.BindingExpressions.object;
import static uk.co.strangeskies.reflection.AnnotatedWildcardTypes.wildcard;
import static uk.co.strangeskies.reflection.token.TypeToken.forAnnotatedType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import uk.co.strangeskies.mathematics.Interval;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.BaseSchema;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.Models;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.collections.CollectionsSchema;
import uk.co.strangeskies.modabi.schema.meta.SchemaBuilder;
import uk.co.strangeskies.reflection.Annotations;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypeToken.Infer;

public class CollectionsSchemaImpl implements CollectionsSchema {
  private final Schema collectionsSchema;

  public CollectionsSchemaImpl(SchemaBuilder schemaBuilder, BaseSchema baseSchema) {
    schemaBuilder = schemaBuilder.name(COLLECTIONS_SCHEMA).dependencies(baseSchema);

    schemaBuilder = schemaBuilder
        .addModel()
        .name(LIST_MODEL)
        .type(new @Infer TypeToken<List<?>>() {})
        .addChild(
            c -> c
                .name("element")
                .type(forAnnotatedType(wildcard(Annotations.from(Infer.class))))
                .input(
                    object().assign(invokeConstructor(ArrayList.class)).invoke("add", boundValue()))
                .output(object().iterate())
                .bindingConstraint(occurrences(Interval.leftBounded(0))))
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(SET_MODEL)
        .type(new @Infer TypeToken<Set<?>>() {})
        .addChild(
            c -> c
                .name("element")
                .type(forAnnotatedType(wildcard(Annotations.from(Infer.class))))
                .input(
                    object().assign(invokeConstructor(HashSet.class)).invoke("add", boundValue()))
                .output(object().iterate())
                .bindingConstraint(occurrences(leftBounded(0))))
        .endModel();

    collectionsSchema = schemaBuilder.create();
  }

  @SuppressWarnings("unchecked")
  private <T> Model<T> getModel(QualifiedName name) {
    return (Model<T>) collectionsSchema.models().get(name);
  }

  @Override
  public Model<List<?>> listModel() {
    return getModel(LIST_MODEL);
  }

  @Override
  public Model<Set<?>> setModel() {
    return getModel(SET_MODEL);
  }

  @Override
  public Model<Map<?, ?>> mapModel() {
    return getModel(MAP_MODEL);
  }

  /* Schema */

  @Override
  public QualifiedName name() {
    return collectionsSchema.name();
  }

  @Override
  public Stream<Schema> dependencies() {
    return collectionsSchema.dependencies();
  }

  @Override
  public Models models() {
    return collectionsSchema.models();
  }

  @Override
  public boolean equals(Object obj) {
    return collectionsSchema.equals(obj);
  }

  @Override
  public int hashCode() {
    return collectionsSchema.hashCode();
  }

  @Override
  public String toString() {
    return name().toString();
  }
}
