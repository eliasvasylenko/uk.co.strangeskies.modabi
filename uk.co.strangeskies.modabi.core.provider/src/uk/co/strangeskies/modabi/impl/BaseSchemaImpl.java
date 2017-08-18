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
package uk.co.strangeskies.modabi.impl;

import static uk.co.strangeskies.modabi.schema.bindingconditions.OccurrencesCondition.occurrences;
import static uk.co.strangeskies.modabi.schema.expression.Expressions.invokeStatic;
import static uk.co.strangeskies.reflection.AnnotatedWildcardTypes.wildcard;
import static uk.co.strangeskies.reflection.Types.wrapPrimitive;
import static uk.co.strangeskies.reflection.token.ExecutableToken.forStaticMethod;
import static uk.co.strangeskies.reflection.token.MethodMatcher.anyConstructor;
import static uk.co.strangeskies.reflection.token.MethodMatcher.anyMethod;
import static uk.co.strangeskies.reflection.token.OverloadResolver.resolveOverload;
import static uk.co.strangeskies.reflection.token.TypeToken.forAnnotatedType;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.w3c.dom.ranges.Range;

import uk.co.strangeskies.mathematics.Interval;
import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.Models;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.SchemaConfigurator;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.DataItem;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.io.Primitive;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.provisions.DereferenceSource;
import uk.co.strangeskies.modabi.processing.provisions.ImportSource;
import uk.co.strangeskies.modabi.processing.provisions.ImportTarget;
import uk.co.strangeskies.modabi.processing.provisions.IncludeTarget;
import uk.co.strangeskies.modabi.processing.provisions.ReferenceTarget;
import uk.co.strangeskies.modabi.schema.DataLoader;
import uk.co.strangeskies.modabi.schema.IOConfigurator;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;
import uk.co.strangeskies.modabi.schema.expression.Expressions;
import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.Annotations;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.Types;
import uk.co.strangeskies.reflection.token.MethodMatcher;
import uk.co.strangeskies.reflection.token.TypeArgument;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypeToken.Infer;
import uk.co.strangeskies.utility.Enumeration;

public class BaseSchemaImpl implements BaseSchema {
  private interface ModelHelper {
    <T> Model<T> apply(String name, Function<ModelConfigurator, Model<T>> type);
  }

  private final Model<Object> referenceModel;
  private final Model<Object> bindingReferenceModel;
  private final Model<DataSource> bufferedDataModel;
  private final Model<DataItem<?>> bufferedDataItemModel;

  private final Model<Package> packageModel;
  private final Model<Class<?>> classModel;
  private final Model<Type> typeModel;
  private final Model<AnnotatedType> annotatedTypeModel;
  private final Model<TypeToken<?>> typeTokenModel;
  private final Model<Enum<?>> enumModel;
  private final Model<Enumeration<?>> enumerationModel;
  private final Model<Interval<Integer>> rangeModel;
  private final Model<Object[]> arrayModel;
  private final Model<Collection<?>> collectionModel;
  private final Model<List<?>> listModel;
  private final Model<Set<?>> setModel;
  private final Model<Object> importModel;
  private final Model<Collection<?>> includeModel;
  private final Model<URI> uriModel;
  private final Model<URL> urlModel;

  private final Model<Map<?, ?>> mapModel;

  private final Schema baseSchema;

  private final Map<Primitive<?>, Model<?>> primitives;

  public BaseSchemaImpl(SchemaBuilder schemaBuilder, DataLoader loader) {
    QualifiedName name = BaseSchema.QUALIFIED_NAME;
    Namespace namespace = name.getNamespace();

    /*
     * Schema
     */
    SchemaConfigurator schemaConfigurator = schemaBuilder.configure(loader).qualifiedName(name);

    /*
     * Models
     */
    ModelHelper modelFactory = new ModelHelper() {
      @Override
      public <T> Model<T> apply(String name, Function<ModelConfigurator, Model<T>> type) {
        return type.apply(schemaConfigurator.addModel().name(name, namespace));
      }
    };

    Model<Enumeration<?>> enumerationBaseType = modelFactory.apply(
        "enumerationBase",
        c -> c
            .export(false)
            .type(new @Infer TypeToken<Enumeration<?>>() {})
            .concrete(false)
            .endNode());

    Model<Object> primitive = modelFactory.apply(
        "primitive",
        p -> p
            .export(false)
            .type(Object.class)
            .concrete(false)
            .addChildBindingPoint(
                c -> c
                    .name("dataType")
                    .model(enumerationBaseType)
                    .type(new @Infer TypeToken<Primitive<?>>() {})
                    .input(IOConfigurator::none)
                    .output(IOConfigurator::none)
                    .override()
                    .concrete(false)
                    .valueResolution(ValueResolution.DECLARATION_TIME)
                    .endNode())
            .addChildBindingPoint(
                c -> c
                    .name("io")
                    .type(void.class)
                    .input(
                        i -> i.target().assign(
                            i
                                .provide(DataSource.class)
                                .invoke(anyMethod().named("get"), i.bound("dataType"))))
                    .output(
                        o -> o.provide(DataTarget.class).invoke(
                            anyMethod().named("put").returning(void.class),
                            o.bound("dataType"),
                            o.source())))
            .endNode());

    primitives = new HashMap<>();
    for (Primitive<?> dataType : Enumeration.getConstants(Primitive.class)) {
      primitives.put(
          dataType,
          modelFactory.apply(
              dataType.name(),
              p -> p
                  .baseModel(dataType.dataClass(), primitive)
                  .addChildBindingPoint(
                      c -> c
                          .name("dataType")
                          .type(resolvePrimitiveDataType(dataType))
                          .override()
                          .provideValue(
                              new BufferingDataTarget()
                                  .put(Primitive.STRING, dataType.name())
                                  .buffer())
                          .endNode())
                  .endNode()));
    }

    arrayModel = modelFactory.apply(
        "array",
        t -> t
            .type(new @Infer TypeToken<Object[]>() {})
            .initializeInput(i -> i.provide(new TypeToken<List<?>>() {}))
            .initializeOutput(
                o -> invokeStatic(Arrays.class, anyMethod().named("asList"), o.parent()))
            .addChildBindingPoint(
                c -> c
                    .name("element")
                    .type(forAnnotatedType(wildcard(Annotations.from(Infer.class))))
                    .input(i -> i.target().invoke(anyMethod().named("add"), i.result()))
                    .output(o -> o.iterate(o.source()))
                    .bindingCondition(occurrences(Interval.bounded(0, null))))
            .addChildBindingPoint(
                c -> c
                    .name("toArray")
                    .type(void.class)
                    .input(i -> i.target().assign(i.target().invoke(anyMethod().named("toArray"))))
                    .output(IOConfigurator::none))
            .endNode());

    collectionModel = modelFactory.apply(
        "collection",
        t -> t
            .type(new @Infer TypeToken<Collection<?>>() {})
            .initializeInput(i -> i.provide())
            .addChildBindingPoint(
                c -> c
                    .name("element")
                    .type(forAnnotatedType(wildcard(Annotations.from(Infer.class))))
                    .input(i -> i.target().invoke(anyMethod().named("add"), i.result()))
                    .output(o -> o.iterate(o.source()))
                    .bindingCondition(occurrences(Interval.bounded(0, null))))
            .endNode());

    listModel = modelFactory.apply(
        "list",
        t -> t.baseModel(new @Infer TypeToken<List<?>>() {}, collectionModel).endNode());

    setModel = modelFactory.apply(
        "set",
        t -> t.baseModel(new @Infer TypeToken<Set<?>>() {}, collectionModel).endNode());

    uriModel = modelFactory.apply(
        "uri",
        t -> t
            .type(URI.class)
            .addChildBindingPoint(
                u -> u
                    .name("uriString")
                    .model(primitive(Primitive.STRING))
                    .input(
                        i -> i.target().assign(
                            Expressions.invokeStatic(URI.class, anyConstructor(), i.result())))
                    .output(o -> o.source().invoke(anyMethod().named("toString"))))
            .endNode());

    urlModel = modelFactory.apply(
        "url",
        t -> t
            .type(URL.class)
            .addChildBindingPoint(
                u -> u
                    .name("urlString")
                    .model(primitive(Primitive.STRING))
                    .input(i -> i.target().assign(invokeStatic(URL.class, anyMethod(), i.result())))
                    .output(
                        o -> o.source().invoke(
                            anyMethod().returning(String.class).named("toString"))))
            .endNode());

    bufferedDataModel = modelFactory.apply(
        "bufferedData",
        t -> t
            .type(DataSource.class)
            .initializeInput(i -> i.provide(DataSource.class))
            .initializeOutput(
                o -> o.parent().invoke(anyMethod().named("pipe"), o.provide(DataTarget.class)))
            .endNode());

    bufferedDataItemModel = modelFactory.apply(
        "bufferedDataItem",
        t -> t
            .type(new TypeToken<DataItem<?>>() {})
            .initializeInput(i -> i.provide(DataSource.class).invoke(anyMethod().named("get")))
            .initializeOutput(
                o -> o.provide(DataTarget.class).invoke(anyMethod().named("put"), o.parent()))
            .endNode());

    @SuppressWarnings("unchecked")
    Model<Object> referenceBaseModel = (Model<Object>) modelFactory.apply(
        "referenceBase",
        t -> t
            .export(false)
            .type(forAnnotatedType(wildcard(Annotations.from(Infer.class))))
            .concrete(false)
            .addChildBindingPoint(
                d -> d
                    .name("targetModel")
                    .input(IOConfigurator::none)
                    .output(IOConfigurator::none)
                    .type(new @Infer TypeToken<Model<?>>() {})
                    .override()
                    .valueResolution(ValueResolution.DECLARATION_TIME)
                    .endNode())
            .addChildBindingPoint(
                d -> d
                    .name("targetId")
                    .input(IOConfigurator::none)
                    .output(IOConfigurator::none)
                    .model(listModel)
                    .override()
                    .concrete(false)
                    .valueResolution(ValueResolution.DECLARATION_TIME)
                    .addChildBindingPoint(
                        e -> e.name("element").model(primitives.get(Primitive.QUALIFIED_NAME)))
                    .endNode())
            .addChildBindingPoint(
                d -> d
                    .name("data")
                    .model(bufferedDataModel)
                    .input(
                        i -> i.target().assign(
                            i.provide(DereferenceSource.class).invoke(
                                anyMethod().named("dereference"),
                                i.bound("targetModel"),
                                i.bound("targetId"),
                                i.result())))
                    .output(
                        o -> o.provide(ReferenceTarget.class).invoke(
                            anyMethod().named("reference").returning(DataSource.class),
                            o.bound("targetModel"),
                            o.bound("targetId"),
                            o.source())))
            .endNode());

    referenceModel = modelFactory.apply(
        "reference",
        t -> t
            .baseModel(referenceBaseModel)
            .concrete(false)
            .addChildBindingPoint(
                c -> c
                    .name("targetModel")
                    .model(referenceBaseModel)
                    .type(new @Infer TypeToken<Model<?>>() {})
                    .override()
                    .concrete(false)
                    .addChildBindingPoint(
                        d -> d
                            .name("targetModel")
                            .model(referenceBaseModel)
                            .type(new @Infer TypeToken<Model<?>>() {})
                            .override()
                            .provideValue(
                                new BufferingDataTarget()
                                    .put(
                                        Primitive.QUALIFIED_NAME,
                                        new QualifiedName("model", namespace))
                                    .buffer())
                            .addChildBindingPoint(
                                e -> e
                                    .name("targetModel")
                                    .model(referenceBaseModel)
                                    .type(new @Infer TypeToken<Model<?>>() {})
                                    .override()
                                    .concrete(false)
                                    .provideValue(
                                        new BufferingDataTarget()
                                            .put(
                                                Primitive.QUALIFIED_NAME,
                                                new QualifiedName("model", namespace))
                                            .buffer())
                                    .endNode())
                            .addChildBindingPoint(
                                e -> e
                                    .name("targetId")
                                    .override()
                                    .provideValue(
                                        new BufferingDataTarget()
                                            .put(
                                                Primitive.QUALIFIED_NAME,
                                                new QualifiedName("configurator", namespace))
                                            .put(
                                                Primitive.QUALIFIED_NAME,
                                                new QualifiedName("name", namespace))
                                            .buffer())
                                    .endNode())
                            .endNode())
                    .addChildBindingPoint(
                        d -> d
                            .name("targetId")
                            .override()
                            .provideValue(
                                new BufferingDataTarget()
                                    .put(
                                        Primitive.QUALIFIED_NAME,
                                        new QualifiedName("configurator", namespace))
                                    .put(
                                        Primitive.QUALIFIED_NAME,
                                        new QualifiedName("name", namespace))
                                    .buffer())
                            .endNode())
                    .endNode())
            .endNode());

    @SuppressWarnings("unchecked")
    Model<Object> bindingReferenceModel = (Model<Object>) modelFactory.apply(
        "bindingReference",
        t -> t
            .type(forAnnotatedType(wildcard(Annotations.from(Infer.class))))
            .concrete(false)
            .initializeInput(i -> i.provide(DereferenceSource.class))
            .addChildBindingPoint(
                c -> c
                    .name("targetNode")
                    .model(referenceModel)
                    .input(IOConfigurator::none)
                    .output(IOConfigurator::none)
                    .override()
                    .addChildBindingPoint(
                        d -> d
                            .name("targetModel")
                            .override()
                            .provideValue(
                                new BufferingDataTarget()
                                    .put(
                                        Primitive.QUALIFIED_NAME,
                                        new QualifiedName("binding", namespace))
                                    .buffer())
                            .endNode())
                    .addChildBindingPoint(
                        e -> e
                            .name("targetId")
                            .override()
                            .provideValue(
                                new BufferingDataTarget()
                                    .put(
                                        Primitive.QUALIFIED_NAME,
                                        new QualifiedName("configurator", namespace))
                                    .put(
                                        Primitive.QUALIFIED_NAME,
                                        new QualifiedName("name", namespace))
                                    .buffer())
                            .endNode())
                    .endNode())
            .endNode());
    this.bindingReferenceModel = bindingReferenceModel;

    packageModel = modelFactory.apply(
        "package",
        t -> t
            .type(Package.class)
            .addChildBindingPoint(
                p -> p
                    .name("name")
                    .model(primitive(Primitive.STRING))
                    .input(i -> invokeStatic(Package.class, anyMethod().named("getPackage")))
                    .output(
                        o -> o.source().invoke(
                            anyMethod().named("getName").returning(String.class))))
            .endNode());

    typeModel = modelFactory.apply(
        "type",
        t -> t
            .type(Type.class)
            .addChildBindingPoint(
                p -> p
                    .name("name")
                    .model(primitive(Primitive.STRING))
                    .input(
                        i -> invokeStatic(Types.class, anyMethod().named("fromString"), i.result()))
                    .output(
                        o -> invokeStatic(
                            Types.class,
                            anyMethod().named("toString").returning(String.class),
                            o.source())))
            .endNode());

    classModel = modelFactory
        .apply("class", t -> t.baseModel(new TypeToken<Class<?>>() {}, typeModel).endNode());

    annotatedTypeModel = modelFactory.apply(
        "annotatedType",
        t -> t
            .type(AnnotatedType.class)
            .addChildBindingPoint(
                p -> p
                    .name("name")
                    .model(primitive(Primitive.STRING))
                    .input(
                        i -> invokeStatic(
                            AnnotatedTypes.class,
                            anyMethod().named("fromString"),
                            i.result()))
                    .output(
                        o -> invokeStatic(
                            AnnotatedTypes.class,
                            anyMethod().named("toString").returning(String.class),
                            o.source())))
            .endNode());

    typeTokenModel = modelFactory.apply(
        "typeToken",
        t -> t
            .type(new TypeToken<TypeToken<?>>() {})
            .addChildBindingPoint(
                c -> c
                    .model(annotatedTypeModel)
                    .input(
                        i -> invokeStatic(
                            TypeToken.class,
                            anyMethod().named("overAnnotatedType"),
                            i.result()))
                    .output(o -> o.source().invoke(anyMethod().named("getAnnotatedDeclaration"))))
            .endNode());

    enumModel = modelFactory.apply(
        "enum",
        t -> t
            .type(new TypeToken<Enum<?>>() {})
            .concrete(false)
            .addChildBindingPoint(
                c -> c
                    .name("enumType")
                    .input(IOConfigurator::none)
                    .output(IOConfigurator::none)
                    .type(new TypeToken<Class<? extends Enum<?>>>() {})
                    .provideValue(new BufferingDataTarget().buffer())
                    .initializeInput(
                        i -> i
                            .provide(ProcessingContext.class)
                            .invokeResolvedMethod("getBindingNode")
                            .invokeResolvedMethod("dataType")
                            .invokeResolvedMethod("getRawType"))
                    .endNode())
            .addChildBindingPoint(
                p -> p
                    .name("name")
                    .input(
                        i -> invokeResolvedStatic(
                            Enumeration.class,
                            "valueOfEnum",
                            i.bound("enumType"),
                            i.result()))
                    .output(o -> o.source().invokeResolvedMethod("getName"))
                    .type(primitives.get(Primitive.STRING))
                    .endNode())
            .endNode());

    enumerationModel = modelFactory.apply(
        "enumeration",
        t -> t
            .baseModel(enumerationBaseType)
            .concrete(false)
            .addChildBindingPoint(
                c -> c
                    .name("enumType")
                    .input(IOConfigurator::none)
                    .output(IOConfigurator::none)
                    .type(new TypeToken<Class<? extends Enumeration<?>>>() {})
                    .provideValue(new BufferingDataTarget().buffer())
                    .initializeInput(
                        i -> i
                            .provide(ProcessingContext.class)
                            .invokeResolvedMethod("getBindingNode")
                            .invokeResolvedMethod("dataType")
                            .invokeResolvedMethod("getRawType"))
                    .endNode())
            .addChildBindingPoint(
                p -> p
                    .name("name")
                    .input(
                        i -> invokeResolvedStatic(
                            Enumeration.class,
                            "valueOf",
                            i.bound("enumType"),
                            i.result()))
                    .output(o -> o.source().invokeResolvedMethod("getName"))
                    .type(primitives.get(Primitive.STRING))
                    .endNode())
            .endNode());

    rangeModel = modelFactory.apply(
        "range",
        t -> t
            .type(new TypeToken<Interval<Integer>>() {})
            .addChildBindingPoint(
                p -> p
                    .name("string")
                    .input(i -> invokeResolvedStatic(Range.class, "parse", i.result()))
                    .output(o -> invokeResolvedStatic(Range.class, "compose", o.source()))
                    .type(primitives.get(Primitive.STRING))
                    .endNode())
            .endNode());

    includeModel = modelFactory.apply(
        "include",
        t -> t
            .type(new @Infer TypeToken<Collection<?>>() {})
            .concrete(false)
            .initializeInput(i -> i.parent())
            .initializeOutput(o -> o.parent())
            .addChildBindingPoint(
                c -> c
                    .name("targetModel")
                    .input(IOConfigurator::none)
                    .output(IOConfigurator::none)
                    .type(new TypeToken<Model<?>>() {})
                    .concrete(false)
                    .baseModel((Model<Object>) referenceModel)
                    .valueResolution(ValueResolution.DECLARATION_TIME)
                    .addChildBindingPoint(
                        d -> d
                            .name("targetModel")
                            .override()
                            .provideValue(
                                new BufferingDataTarget()
                                    .put(
                                        Primitive.QUALIFIED_NAME,
                                        new QualifiedName("model", namespace))
                                    .buffer())
                            .endNode())
                    .addChildBindingPoint(
                        d -> d
                            .name("targetId")
                            .override()
                            .provideValue(
                                new BufferingDataTarget()
                                    .put(
                                        Primitive.QUALIFIED_NAME,
                                        new QualifiedName("name", namespace))
                                    .buffer())
                            .endNode())
                    .endNode())
            .addChildBindingPoint(
                e -> e
                    .name("object")
                    .input(i -> invokeResolvedStatic(IncludeTarget.class, "include", i.target()))
                    .output(
                        o -> invokeResolvedStatic(
                            IncludeTarget.class,
                            "include",
                            o.bound("targetModel")))
                    .type(Collection.class)
                    .endNode())
            .endNode());

    importModel = modelFactory.apply(
        "import",
        t -> t
            .type(Object.class)
            .concrete(false)
            .initializeInput(i -> i.parent())
            .initializeOutput(o -> o.parent())
            .addChildBindingPoint(
                c -> c
                    .name("targetModel")
                    .input(IOConfigurator::none)
                    .output(IOConfigurator::none)
                    .type(new TypeToken<Model<?>>() {})
                    .baseModel((Model<Object>) referenceModel)
                    .concrete(false)
                    .valueResolution(ValueResolution.DECLARATION_TIME)
                    .addChildBindingPoint(
                        d -> d
                            .name("targetModel")
                            .override()
                            .provideValue(
                                new BufferingDataTarget()
                                    .put(
                                        Primitive.QUALIFIED_NAME,
                                        new QualifiedName("model", namespace))
                                    .buffer())
                            .endNode())
                    .addChildBindingPoint(
                        d -> d
                            .name("targetId")
                            .override()
                            .provideValue(
                                new BufferingDataTarget()
                                    .put(
                                        Primitive.QUALIFIED_NAME,
                                        new QualifiedName("name", namespace))
                                    .buffer())
                            .endNode())
                    .endNode())
            .addChildBindingPoint(
                d -> d
                    .name("targetId")
                    .input(IOConfigurator::none)
                    .output(IOConfigurator::none)
                    .type(listModel)
                    .concrete(false)
                    .valueResolution(ValueResolution.DECLARATION_TIME)
                    .addChildBindingPoint(
                        e -> e
                            .name("element")
                            .type(primitives.get(Primitive.QUALIFIED_NAME))
                            .endNode())
                    .endNode())
            .addChildBindingPoint(
                d -> d
                    .name("data")
                    .input(
                        i -> i.provide(ImportSource.class).invokeResolvedMethod(
                            "dereferenceImport",
                            i.bound("targetModel"),
                            i.bound("targetId"),
                            i.result()))
                    .output(
                        o -> o.provide(ImportTarget.class).invokeResolvedMethod(
                            "referenceImport",
                            o.bound("targetModel"),
                            o.bound("targetId"),
                            o.source()))
                    .type(bufferedDataModel)
                    .endNode())
            .endNode());

    /*
     * Having trouble annotating Map.Entry for some reason, so need this kludge.
     */
    mapModel = modelFactory.apply(
        "map",
        c -> c
            .type(new @Infer TypeToken<Map<?, ?>>() {})
            .addChildBindingPoint(
                f -> f
                    .name("entry")
                    .bindingCondition(occurrences(Interval.bounded(0, null)))
                    .input(IOConfigurator::none)
                    .output(o -> o.iterate(o.source().invokeResolvedMethod("entrySet")))
                    .type(void.class)
                    .initializeInput(i -> i.parent())
                    .addChildBindingPoint(
                        k -> k
                            .name("key")
                            .input(IOConfigurator::none)
                            .output(o -> o.source().invokeResolvedMethod("getKey"))
                            .type(forAnnotatedType(wildcard(Annotations.from(Infer.class))))
                            .extensible(true)
                            .endNode())
                    .addChildBindingPoint(
                        v -> v
                            .name("value")
                            .output(o -> o.source().invokeResolvedMethod("getValue"))
                            .input(
                                i -> i
                                    .target()
                                    .invokeResolvedMethod("put", i.bound("key"), i.result()))
                            .type(forAnnotatedType(wildcard(Annotations.from(Infer.class))))
                            .extensible(true)
                            .endNode())
                    .endNode())
            .endNode());

    /*
     * Schema
     */
    baseSchema = schemaConfigurator.create();
  }

  private <T> TypeToken<Primitive<T>> resolvePrimitiveDataType(Primitive<T> dataType) {
    return new TypeToken<Primitive<T>>() {}
        .withTypeArguments(new TypeArgument<T>(wrapPrimitive(dataType.dataClass())) {});
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> Model<T> primitive(Primitive<T> type) {
    return (Model<T>) primitives.get(type);
  }

  @Override
  public Model<Package> packageModel() {
    return packageModel;
  }

  @Override
  public Model<Class<?>> classModel() {
    return classModel;
  }

  @Override
  public Model<Type> typeModel() {
    return typeModel;
  }

  @Override
  public Model<AnnotatedType> annotatedTypeModel() {
    return annotatedTypeModel;
  }

  @Override
  public Model<TypeToken<?>> typeTokenModel() {
    return typeTokenModel;
  }

  @Override
  public Model<Enum<?>> enumModel() {
    return enumModel;
  }

  @Override
  public Model<Enumeration<?>> enumerationModel() {
    return enumerationModel;
  }

  @Override
  public Model<Interval<Integer>> rangeModel() {
    return rangeModel;
  }

  @Override
  public Model<?> referenceModel() {
    return referenceModel;
  }

  @Override
  public Model<?> bindingReferenceModel() {
    return bindingReferenceModel;
  }

  @Override
  public Model<DataSource> bufferedDataModel() {
    return bufferedDataModel;
  }

  @Override
  public Model<DataItem<?>> bufferedDataItemModel() {
    return bufferedDataItemModel;
  }

  @Override
  public Model<Object[]> arrayModel() {
    return arrayModel;
  }

  @Override
  public Model<Collection<?>> collectionModel() {
    return collectionModel;
  }

  @Override
  public Model<List<?>> listModel() {
    return listModel;
  }

  @Override
  public Model<Set<?>> setModel() {
    return setModel;
  }

  @Override
  public Model<Collection<?>> includeModel() {
    return includeModel;
  }

  @Override
  public Model<Object> importModel() {
    return importModel;
  }

  @Override
  public Model<URI> uriModel() {
    return uriModel;
  }

  @Override
  public Model<URL> urlModel() {
    return urlModel;
  }

  @Override
  public Model<Map<?, ?>> mapModel() {
    return mapModel;
  }

  public static <K, V> Map.Entry<K, V> mapEntry(K key, V value) {
    return null;
  }

  /* Schema */

  @Override
  public QualifiedName qualifiedName() {
    return baseSchema.qualifiedName();
  }

  @Override
  public Schemata dependencies() {
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
  public Imports imports() {
    return Imports.empty();
  }

  @Override
  public String toString() {
    return qualifiedName().toString();
  }
}
