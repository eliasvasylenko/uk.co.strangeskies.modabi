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
import static uk.co.strangeskies.modabi.expression.Expressions.invokeStatic;
import static uk.co.strangeskies.modabi.schema.BindingConditionPrototype.occurrences;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.boundValue;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.object;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.parent;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.provide;
import static uk.co.strangeskies.reflection.AnnotatedWildcardTypes.wildcard;
import static uk.co.strangeskies.reflection.token.TypeToken.forAnnotatedType;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import uk.co.strangeskies.mathematics.Interval;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.expression.Expressions;
import uk.co.strangeskies.modabi.schema.BaseSchema;
import uk.co.strangeskies.modabi.schema.BindingContext;
import uk.co.strangeskies.modabi.schema.BindingExpressions;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.Models;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.meta.SchemaBuilder;
import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.Annotations;
import uk.co.strangeskies.reflection.Types;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypeToken.Infer;
import uk.co.strangeskies.utility.Enumeration;

public class BaseSchemaImpl implements BaseSchema {
  private final Schema baseSchema;

  public BaseSchemaImpl(SchemaBuilder schemaBuilder) {
    schemaBuilder = schemaBuilder.name(BASE_SCHEMA);

    schemaBuilder = schemaBuilder
        .addModel()
        .name(ROOT_MODEL)
        .type(Object.class)
        .partial()
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(STRING_MODEL)
        .type(String.class)
        .addChild()
        .name(STRING_MODEL)
        .input(provide(new TypeToken<Supplier<String>>() {}).invoke("get"))
        .output(provide(new TypeToken<Consumer<String>>() {}).invoke("accept", object()))
        .endChild()
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(BINARY_MODEL)
        .type(byte[].class)
        .addChild(
            c -> c
                .model(STRING_MODEL)
                .input(
                    BindingExpressions
                        .object()
                        .assign(
                            invokeStatic(Base64.class, "getDecoder")
                                .invoke("decode", boundValue())))
                .output(
                    invokeStatic(Base64.class, "getEncoder").invoke("encodeToString", object())))
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(INTEGER_MODEL)
        .type(BigInteger.class)
        .addChild(
            c -> c
                .model(STRING_MODEL)
                .input(object().assign(invokeConstructor(BigInteger.class, boundValue())))
                .output(object().invoke("toString")))
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(DECIMAL_MODEL)
        .type(BigDecimal.class)
        .addChild(
            c -> c
                .model(STRING_MODEL)
                .input(object().assign(invokeConstructor(BigDecimal.class, boundValue())))
                .output(object().invoke("toString")))
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(INT_MODEL)
        .type(int.class)
        .addChild(
            c -> c
                .model(STRING_MODEL)
                .input(object().assign(invokeStatic(Integer.class, "parseInt", boundValue())))
                .output(object().invoke("toString")))
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(LONG_MODEL)
        .type(long.class)
        .addChild(
            c -> c
                .model(STRING_MODEL)
                .input(object().assign(invokeStatic(Long.class, "parseLong", boundValue())))
                .output(object().invoke("toString")))
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(FLOAT_MODEL)
        .type(float.class)
        .addChild(
            c -> c
                .model(STRING_MODEL)
                .input(object().assign(invokeStatic(Float.class, "parseFloat", boundValue())))
                .output(object().invoke("toString")))
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(DOUBLE_MODEL)
        .type(double.class)
        .addChild(
            c -> c
                .model(STRING_MODEL)
                .input(object().assign(invokeStatic(Double.class, "parseDouble", boundValue())))
                .output(object().invoke("toString")))
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(BOOLEAN_MODEL)
        .type(boolean.class)
        .addChild(
            c -> c
                .model(STRING_MODEL)
                .input(object().assign(invokeStatic(Boolean.class, "parseBoolean", boundValue())))
                .output(object().invoke("toString")))
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(QUALIFIED_NAME_MODEL)
        .type(QualifiedName.class)
        .addChild(
            c -> c
                .model(STRING_MODEL)
                .input(object().assign(invokeStatic(Boolean.class, "parseBoolean", boundValue())))
                .output(object().invoke("toString")))
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(ARRAY_MODEL)
        .type(new @Infer TypeToken<Object[]>() {})
        .addChild()
        .input(provide(new TypeToken<List<?>>() {}))
        .output(invokeStatic(Arrays.class, "asList", parent()))
        .endChild()
        .addChild(
            c -> c
                .name("element")
                .type(forAnnotatedType(wildcard(Annotations.from(Infer.class))))
                .input(object().invoke("add", boundValue()))
                .output(object().iterate())
                .bindingCondition(occurrences(leftBounded(0))))
        .addChild(
            c -> c
                .name("toArray")
                .type(void.class)
                .input(object().assign(object().invoke("toArray")))
                .noOutput())
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(COLLECTION_MODEL)
        .type(new @Infer TypeToken<Collection<?>>() {})
        .addChild()
        .input(provide())
        .endChild()
        .addChild(
            c -> c
                .name("element")
                .type(forAnnotatedType(wildcard(Annotations.from(Infer.class))))
                .input(object().invoke("add", boundValue()))
                .output(object().iterate())
                .bindingCondition(occurrences(leftBounded(0))))
        .endModel()

        .addModel()
        .name(LIST_MODEL)
        .type(new @Infer TypeToken<List<?>>() {})
        .baseModel("collection")
        .endModel()

        .addModel()
        .name(SET_MODEL)
        .type(new @Infer TypeToken<Set<?>>() {})
        .baseModel("collection")
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(URI_MODEL)
        .type(URI.class)
        .addChild(
            u -> u
                .name("uriString")
                .model(STRING_MODEL)
                .input(object().assign(invokeConstructor(URI.class, boundValue())))
                .output(object().invoke("toString")))
        .endModel()

        .addModel()
        .name(URL_MODEL)
        .type(URL.class)
        .addChild(
            u -> u
                .name("urlString")
                .model(STRING_MODEL)
                .input(object().assign(invokeConstructor(URL.class, boundValue())))
                .output(object().invoke("toString")))
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(PACKAGE_MODEL)
        .type(Package.class)
        .addChild(
            p -> p
                .name("name")
                .model(STRING_MODEL)
                .input(invokeStatic(Package.class, "getPackage"))
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
                .input(invokeStatic(Types.class, "fromString", boundValue()))
                .output(invokeStatic(Types.class, "toString", object())))
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(CLASS_MODEL)
        .type(new TypeToken<Class<?>>() {})
        .baseModel(TYPE_MODEL)
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(ANNOTATED_TYPE_MODEL)
        .type(AnnotatedType.class)
        .addChild(
            p -> p
                .name("name")
                .model(STRING_MODEL)
                .input(invokeStatic(AnnotatedTypes.class, "fromString", boundValue()))
                .output(invokeStatic(AnnotatedTypes.class, "toString", object())))
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(TYPE_TOKEN_MODEL)
        .type(new TypeToken<TypeToken<?>>() {})
        .addChild(
            c -> c
                .model(ANNOTATED_TYPE_MODEL)
                .input(invokeStatic(TypeToken.class, "overAnnotatedType", boundValue()))
                .output(object().invoke("getAnnotatedDeclaration")))
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(ENUM_MODEL)
        .type(new TypeToken<Enum<?>>() {})
        .partial()
        .addChild(
            c -> c
                .name("enumType")
                .noInput()
                .noOutput()
                .type(new TypeToken<Class<? extends Enum<?>>>() {})
                .input(
                    provide(BindingContext.class)
                        .invoke("getBindingNode")
                        .invoke("dataType")
                        .invoke("getRawType")))
        .addChild(
            p -> p
                .name("name")
                .input(
                    Expressions
                        .invokeStatic(
                            Enumeration.class,
                            "valueOfEnum",
                            boundValue("enumType"),
                            boundValue()))
                .output(object().invoke("getName"))
                .model(STRING_MODEL))
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(ENUMERATION_MODEL)
        .type(new TypeToken<Enumeration<?>>() {})
        .partial()
        .addChild(
            c -> c
                .name("enumType")
                .noInput()
                .noOutput()
                .type(new TypeToken<Class<? extends Enumeration<?>>>() {})
                .input(
                    provide(BindingContext.class)
                        .invoke("getBindingNode")
                        .invoke("dataType")
                        .invoke("getRawType")))
        .addChild(
            p -> p
                .name("name")
                .input(
                    invokeStatic(
                        Enumeration.class,
                        "valueOf",
                        boundValue("enumType"),
                        boundValue()))
                .output(object().invoke("getName"))
                .model(STRING_MODEL))
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(INTERVAL_MODEL)
        .type(new TypeToken<Interval<Integer>>() {})
        .addChild(
            p -> p
                .name("string")
                .input(invokeStatic(Interval.class, "parse", boundValue()))
                .output(invokeStatic(Interval.class, "compose", object()))
                .model(STRING_MODEL))
        .endModel();

    baseSchema = schemaBuilder.create();
  }

  @SuppressWarnings("unchecked")
  private <T> Model<T> getModel(QualifiedName name) {
    return (Model<T>) baseSchema.models().get(name);
  }

  @Override
  public Model<Object> rootModel() {
    return getModel(ROOT_MODEL);
  }

  @Override
  public Model<String> stringModel() {
    return getModel(STRING_MODEL);
  }

  @Override
  public Model<byte[]> binaryModel() {
    return getModel(BINARY_MODEL);
  }

  @Override
  public Model<BigInteger> integerModel() {
    return getModel(INTEGER_MODEL);
  }

  @Override
  public Model<BigDecimal> decimalModel() {
    return getModel(DECIMAL_MODEL);
  }

  @Override
  public Model<Integer> intModel() {
    return getModel(INT_MODEL);
  }

  @Override
  public Model<Long> longModel() {
    return getModel(LONG_MODEL);
  }

  @Override
  public Model<Float> floatModel() {
    return getModel(FLOAT_MODEL);
  }

  @Override
  public Model<Double> doubleModel() {
    return getModel(DOUBLE_MODEL);
  }

  @Override
  public Model<Boolean> booleanModel() {
    return getModel(BOOLEAN_MODEL);
  }

  @Override
  public Model<QualifiedName> qualifiedNameModel() {
    return getModel(QUALIFIED_NAME_MODEL);
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

  @Override
  public Model<Enum<?>> enumModel() {
    return getModel(ENUM_MODEL);
  }

  @Override
  public Model<Enumeration<?>> enumerationModel() {
    return getModel(ENUMERATION_MODEL);
  }

  @Override
  public Model<Interval<Integer>> intervalModel() {
    return getModel(INTERVAL_MODEL);
  }

  @Override
  public Model<Object[]> arrayModel() {
    return getModel(ARRAY_MODEL);
  }

  @Override
  public Model<Collection<?>> collectionModel() {
    return getModel(COLLECTION_MODEL);
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

  @Override
  public Model<URI> uriModel() {
    return getModel(URI_MODEL);
  }

  @Override
  public Model<URL> urlModel() {
    return getModel(URL_MODEL);
  }

  public static <K, V> Map.Entry<K, V> mapEntry(K key, V value) {
    return null;
  }

  /* Schema */

  @Override
  public QualifiedName name() {
    return baseSchema.name();
  }

  @Override
  public Stream<Schema> dependencies() {
    return baseSchema.dependencies();
  }

  @Override
  public Models models() {
    return baseSchema.models();
  }

  @Override
  public boolean equals(Object obj) {
    return baseSchema.equals(obj);
  }

  @Override
  public int hashCode() {
    return baseSchema.hashCode();
  }

  @Override
  public String toString() {
    return name().toString();
  }
}
