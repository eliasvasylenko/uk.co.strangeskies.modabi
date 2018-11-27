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

import static uk.co.strangeskies.modabi.expression.Expressions.invokeConstructor;
import static uk.co.strangeskies.modabi.expression.Expressions.invokeStatic;
import static uk.co.strangeskies.modabi.schema.BaseSchema.STRING_MODEL;
import static uk.co.strangeskies.modabi.schema.meta.BindingExpressions.boundValue;
import static uk.co.strangeskies.modabi.schema.meta.BindingExpressions.object;
import static uk.co.strangeskies.modabi.schema.meta.BindingExpressions.provide;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.BaseSchema;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.Models;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.meta.SchemaBuilder;
import uk.co.strangeskies.modabi.schema.types.ReflectionSchema;
import uk.co.strangeskies.modabi.schema.types.TypesSchema;
import uk.co.strangeskies.reflection.AnnotatedTypeParser;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypeParser;
import uk.co.strangeskies.reflection.token.TypeToken;

public class TypesSchemaImpl implements TypesSchema {
  private final Schema collectionsSchema;

  public TypesSchemaImpl(SchemaBuilder schemaBuilder, BaseSchema baseSchema) {
    schemaBuilder = schemaBuilder.name(REFLECTION_SCHEMA).dependencies(baseSchema);

    schemaBuilder = schemaBuilder
        .addModel()
        .name(PACKAGE_MODEL)
        .type(Package.class)
        .addChild(
            p -> p
                .name("name")
                .model(STRING_MODEL)
                .input(invokeStatic(Package.class, "getPackage", boundValue()))
                .output(object().invoke("getName")))
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(TYPE_MODEL)
        .type(Type.class)
        .addChild(
            p -> p
                .name("name")
                .model(STRING_MODEL)
                .input(
                    invokeConstructor(TypeParser.class, provide(Imports.class))
                        .invoke("type")
                        .invoke("parse", boundValue()))
                .output(invokeConstructor(TypeParser.class, provide(Imports.class)).invoke("toString", object())))
        .endModel();

    schemaBuilder = schemaBuilder.addModel().name(CLASS_MODEL).type(new TypeToken<Class<?>>() {
    }).baseModel(TYPE_MODEL).endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(ANNOTATED_TYPE_MODEL)
        .type(AnnotatedType.class)
        .addChild(
            p -> p
                .name("name")
                .model(STRING_MODEL)
                .input(
                    invokeConstructor(AnnotatedTypeParser.class, provide(Imports.class))
                        .invoke("type")
                        .invoke("parse", boundValue()))
                .output(
                    invokeConstructor(AnnotatedTypeParser.class, provide(Imports.class)).invoke("toString", object())))
        .endModel();

    schemaBuilder = schemaBuilder.addModel().name(TYPE_TOKEN_MODEL).type(new TypeToken<TypeToken<?>>() {
    })
        .addChild(
            c -> c
                .model(ANNOTATED_TYPE_MODEL)
                .input(invokeStatic(TypeToken.class, "forAnnotatedType", boundValue()))
                .output(object().invoke("getAnnotatedDeclaration")))
        .endModel();

    collectionsSchema = schemaBuilder.create();
  }

  @SuppressWarnings("unchecked")
  private <T> Model<T> getModel(QualifiedName name) {
    return (Model<T>) collectionsSchema.models().get(name);
  }

  @Override
  public Model<Package> packageModel() {
    return getModel(PACKAGE_MODEL);
  }

  @Override
  public Model<Class<?>> classModel() {
    return getModel(CLASS_MODEL);
  }

  @Override
  public Model<Type> typeModel() {
    return getModel(TYPE_MODEL);
  }

  @Override
  public Model<AnnotatedType> annotatedTypeModel() {
    return getModel(ANNOTATED_TYPE_MODEL);
  }

  @Override
  public Model<TypeToken<?>> typeTokenModel() {
    return getModel(TYPE_TOKEN_MODEL);
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
