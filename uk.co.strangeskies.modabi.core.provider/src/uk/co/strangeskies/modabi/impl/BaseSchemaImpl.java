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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static uk.co.strangeskies.modabi.schema.bindingconditions.OccurrencesCondition.occurrences;
import static uk.co.strangeskies.modabi.schema.expression.Expressions.invokeConstructor;
import static uk.co.strangeskies.modabi.schema.expression.Expressions.invokeStatic;
import static uk.co.strangeskies.reflection.AnnotatedWildcardTypes.wildcard;
import static uk.co.strangeskies.reflection.token.TypeToken.forAnnotatedType;
import static uk.co.strangeskies.reflection.token.TypedObject.typedObject;

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
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.w3c.dom.ranges.Range;

import uk.co.strangeskies.mathematics.Interval;
import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.provisions.ImportReader;
import uk.co.strangeskies.modabi.processing.provisions.ImportWriter;
import uk.co.strangeskies.modabi.processing.provisions.IncludeWriter;
import uk.co.strangeskies.modabi.processing.provisions.ReferenceReader;
import uk.co.strangeskies.modabi.processing.provisions.ReferenceWriter;
import uk.co.strangeskies.modabi.schema.IOBuilder;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelBuilder;
import uk.co.strangeskies.modabi.schema.Node;
import uk.co.strangeskies.modabi.schema.expression.Expressions;
import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.Annotations;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.Types;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypeToken.Infer;
import uk.co.strangeskies.utility.Enumeration;

public class BaseSchemaImpl implements BaseSchema {
  private interface ModelHelper {
    <T> Model<T> apply(String name, Function<ModelBuilder, Model<T>> type);
  }

  private final Model<String> stringModel;
  private final Model<byte[]> binaryModel;
  private final Model<BigInteger> integerModel;
  private final Model<BigDecimal> decimalModel;
  private final Model<Integer> intModel;
  private final Model<Long> longModel;
  private final Model<Float> floatModel;
  private final Model<Double> doubleModel;
  private final Model<Boolean> booleanModel;
  private final Model<QualifiedName> qualifiedNameModel;

  private final Model<Object> referenceModel;
  private final Model<Object> bindingReferenceModel;
  private final Model<Void> referenceIndexModel;
  private final Model<Object> importModel;

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
  private final Model<URI> uriModel;
  private final Model<URL> urlModel;

  private final Model<Map<?, ?>> mapModel;

  private final Schema baseSchema;

  /*
   * TODO bootstrap proxies to reference models which aren't built yet
   */
  private Model<Model<?>> metaModelProxy;
  private Model<Node<?>> nodeModelProxy;

  public BaseSchemaImpl(SchemaBuilder schemaBuilder) {
    QualifiedName name = BaseSchema.QUALIFIED_NAME;
    Namespace namespace = name.getNamespace();

    /*
     * Schema
     */
    SchemaBuilder schemaConfigurator = schemaBuilder.qualifiedName(name);

    /*
     * Models
     */
    ModelHelper modelFactory = new ModelHelper() {
      @Override
      public <T> Model<T> apply(String name, Function<ModelBuilder, Model<T>> type) {
        return type.apply(schemaConfigurator.addModel().name(name, namespace));
      }
    };

    stringModel = modelFactory.apply(
        "string",
        p -> p
            .baseType(String.class)
            .initializeInput(i -> i.provide(new TypeToken<Supplier<String>>() {}).invoke("get"))
            .initializeOutput(
                o -> o
                    .provide(new TypeToken<Consumer<String>>() {})
                    .invoke("accept", o.outputObject()))
            .endNode());

    binaryModel = modelFactory.apply(
        "binary",
        t -> t
            .baseType(byte[].class)
            .addChildBindingPoint(
                c -> c
                    .model(stringModel)
                    .input(
                        i -> i.target().assign(
                            invokeStatic(Base64.class, "getDecoder").invoke("decode", i.result())))
                    .output(
                        o -> invokeStatic(Base64.class, "getEncoder")
                            .invoke("encodeToString", o.source())))
            .endNode());

    integerModel = modelFactory.apply(
        "integer",
        t -> t
            .baseType(BigInteger.class)
            .addChildBindingPoint(
                c -> c
                    .model(stringModel)
                    .input(i -> i.target().assign(invokeConstructor(BigInteger.class, i.result())))
                    .output(o -> o.source().invoke("toString")))
            .endNode());

    decimalModel = modelFactory.apply(
        "integer",
        t -> t
            .baseType(BigDecimal.class)
            .addChildBindingPoint(
                c -> c
                    .model(stringModel)
                    .input(i -> i.target().assign(invokeConstructor(BigDecimal.class, i.result())))
                    .output(o -> o.source().invoke("toString")))
            .endNode());

    intModel = modelFactory.apply(
        "int",
        t -> t
            .baseType(int.class)
            .addChildBindingPoint(
                c -> c
                    .model(stringModel)
                    .input(
                        i -> i.target().assign(invokeStatic(Integer.class, "parseInt", i.result())))
                    .output(o -> o.source().invoke("toString")))
            .endNode());

    longModel = modelFactory.apply(
        "long",
        t -> t
            .baseType(long.class)
            .addChildBindingPoint(
                c -> c
                    .model(stringModel)
                    .input(
                        i -> i.target().assign(invokeStatic(Long.class, "parseLong", i.result())))
                    .output(o -> o.source().invoke("toString")))
            .endNode());

    floatModel = modelFactory.apply(
        "float",
        t -> t
            .baseType(float.class)
            .addChildBindingPoint(
                c -> c
                    .model(stringModel)
                    .input(
                        i -> i.target().assign(invokeStatic(Float.class, "parseFloat", i.result())))
                    .output(o -> o.source().invoke("toString")))
            .endNode());

    doubleModel = modelFactory.apply(
        "long",
        t -> t
            .baseType(double.class)
            .addChildBindingPoint(
                c -> c
                    .model(stringModel)
                    .input(
                        i -> i.target().assign(
                            invokeStatic(Double.class, "parseDouble", i.result())))
                    .output(o -> o.source().invoke("toString")))
            .endNode());

    booleanModel = modelFactory.apply(
        "long",
        t -> t
            .baseType(boolean.class)
            .addChildBindingPoint(
                c -> c
                    .model(stringModel)
                    .input(
                        i -> i.target().assign(
                            invokeStatic(Boolean.class, "parseBoolean", i.result())))
                    .output(o -> o.source().invoke("toString")))
            .endNode());

    qualifiedNameModel = modelFactory.apply(
        "long",
        t -> t
            .baseType(QualifiedName.class)
            .addChildBindingPoint(
                c -> c
                    .model(stringModel)
                    .input(
                        i -> i.target().assign(
                            invokeStatic(Boolean.class, "parseBoolean", i.result())))
                    .output(o -> o.source().invoke("toString")))
            .endNode());

    arrayModel = modelFactory.apply(
        "array",
        t -> t
            .baseType(new @Infer TypeToken<Object[]>() {})
            .initializeInput(i -> i.provide(new TypeToken<List<?>>() {}))
            .initializeOutput(o -> invokeStatic(Arrays.class, "asList", o.parent()))
            .addChildBindingPoint(
                c -> c
                    .name("element")
                    .type(forAnnotatedType(wildcard(Annotations.from(Infer.class))))
                    .input(i -> i.target().invoke("add", i.result()))
                    .output(o -> o.iterate(o.source()))
                    .bindingCondition(occurrences(Interval.bounded(0, null))))
            .addChildBindingPoint(
                c -> c
                    .name("toArray")
                    .type(void.class)
                    .input(i -> i.target().assign(i.target().invoke("toArray")))
                    .output(IOBuilder::none))
            .endNode());

    collectionModel = modelFactory.apply(
        "collection",
        t -> t
            .baseType(new @Infer TypeToken<Collection<?>>() {})
            .initializeInput(i -> i.provide())
            .addChildBindingPoint(
                c -> c
                    .name("element")
                    .type(forAnnotatedType(wildcard(Annotations.from(Infer.class))))
                    .input(i -> i.target().invoke("add", i.result()))
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
            .baseType(URI.class)
            .addChildBindingPoint(
                u -> u
                    .name("uriString")
                    .model(stringModel)
                    .input(i -> i.target().assign(invokeConstructor(URI.class, i.result())))
                    .output(o -> o.source().invoke("toString")))
            .endNode());

    urlModel = modelFactory.apply(
        "url",
        t -> t
            .baseType(URL.class)
            .addChildBindingPoint(
                u -> u
                    .name("urlString")
                    .model(stringModel)
                    .input(i -> i.target().assign(invokeConstructor(URL.class, i.result())))
                    .output(o -> o.source().invoke("toString")))
            .endNode());

    @SuppressWarnings("unchecked")
    Model<Object> referenceBaseModel = (Model<Object>) modelFactory.apply(
        "referenceBase",
        t -> t
            .export(false)
            .baseType(forAnnotatedType(wildcard(Annotations.from(Infer.class))))
            .concrete(false)
            .addChildBindingPoint(
                d -> d
                    .name("targetModel")
                    .input(IOBuilder::none)
                    .output(IOBuilder::none)
                    .type(new @Infer TypeToken<Model<?>>() {})
                    .override()
                    .endNode())
            .addChildBindingPoint(
                d -> d
                    .name("targetId")
                    .input(IOBuilder::none)
                    .output(IOBuilder::none)
                    .model(listModel)
                    .override()
                    .concrete(false)
                    .addChildBindingPoint(e -> e.name("element").model(qualifiedNameModel))
                    .endNode())
            .addChildBindingPoint(
                d -> d
                    .name("data")
                    .model(stringModel)
                    .input(
                        i -> i.target().assign(
                            i.provide(ReferenceReader.class).invoke(
                                "dereference",
                                i.bound("targetModel"),
                                i.bound("targetId"),
                                i.result())))
                    .output(
                        o -> o.provide(ReferenceWriter.class).invoke(
                            "reference",
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
                                typedObject(new TypeToken<Model<Model<?>>>() {}, metaModelProxy))
                            .addChildBindingPoint(
                                e -> e
                                    .name("targetModel")
                                    .model(referenceBaseModel)
                                    .type(new @Infer TypeToken<Model<?>>() {})
                                    .override()
                                    .concrete(false)
                                    .provideValue(
                                        typedObject(
                                            new TypeToken<Model<Model<?>>>() {},
                                            metaModelProxy))
                                    .endNode())
                            .addChildBindingPoint(
                                e -> e
                                    .name("targetId")
                                    .type(new TypeToken<List<QualifiedName>>() {})
                                    .override()
                                    .provideValue(
                                        typedObject(
                                            new TypeToken<List<QualifiedName>>() {},
                                            asList(
                                                new QualifiedName("configurator", namespace),
                                                new QualifiedName("name", namespace))))
                                    .endNode())
                            .endNode())
                    .addChildBindingPoint(
                        d -> d
                            .name("targetId")
                            .override()
                            .provideValue(
                                typedObject(
                                    new TypeToken<List<QualifiedName>>() {},
                                    asList(
                                        new QualifiedName("configurator", namespace),
                                        new QualifiedName("name", namespace))))
                            .endNode())
                    .endNode())
            .endNode());

    @SuppressWarnings("unchecked")
    Model<Object> bindingReferenceModel = (Model<Object>) modelFactory.apply(
        "bindingReference",
        t -> t
            .baseType(forAnnotatedType(wildcard(Annotations.from(Infer.class))))
            .concrete(false)
            .initializeInput(i -> i.provide(ReferenceReader.class))
            .addChildBindingPoint(
                c -> c
                    .name("targetNode")
                    .model(referenceModel)
                    .input(IOBuilder::none)
                    .output(IOBuilder::none)
                    .override()
                    .addChildBindingPoint(
                        d -> d
                            .name("targetModel")
                            .override()
                            .provideValue(
                                typedObject(new TypeToken<Model<Node<?>>>() {}, nodeModelProxy))
                            .endNode())
                    .addChildBindingPoint(
                        e -> e
                            .name("targetId")
                            .override()
                            .provideValue(
                                typedObject(
                                    new TypeToken<List<QualifiedName>>() {},
                                    asList(
                                        new QualifiedName("configurator", namespace),
                                        new QualifiedName("name", namespace))))
                            .endNode())
                    .endNode())
            .endNode());
    this.bindingReferenceModel = bindingReferenceModel;

    /*
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * TODO this marks binding points as referenceable. Bear in mind this is NOT
     * nodes, it is binding points. For a binding point marked as referenceable
     * using this model, all items bound to that binding point will be added to the
     * ProcessedBindings object in the processing context.
     * 
     * 
     * TODO change of design! Things are only added to the ProcessedBindings object
     * in the context if done explicitly e.g. by using this model.
     * 
     * 
     * 
     * 
     * 
     * TODO the information for how to get an index id from an object, and how to
     * load an index id from a string, should be given here. This means it no longer
     * has to be a part of ReferenceWriter etc.
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     */
    referenceIndexModel = modelFactory.apply(
        "include",
        t -> t
            .baseType(Void.class)
            .concrete(false)
            .addChildBindingPoint(
                c -> c.name("targetBinding").input(IOBuilder::none).output(IOBuilder::none).model(
                    bindingReferenceModel))
            .initializeInput(
                i -> invokeStatic(IncludeWriter.class, "include", i.binding("targetBinding")))
            .initializeOutput(
                o -> invokeStatic(IncludeWriter.class, "include", o.binding("targetBinding")))
            .endNode());

    importModel = modelFactory.apply(
        "import",
        t -> t
            .baseType(Object.class)
            .concrete(false)
            .initializeInput(i -> i.parent())
            .initializeOutput(o -> o.parent())
            .addChildBindingPoint(
                c -> c
                    .name("targetModel")
                    .input(IOBuilder::none)
                    .output(IOBuilder::none)
                    .model(referenceModel)
                    .override()
                    .concrete(false)
                    .addChildBindingPoint(
                        d -> d
                            .name("targetModel")
                            .override()
                            .provideValue(
                                typedObject(new TypeToken<Model<Model<?>>>() {}, metaModelProxy))
                            .endNode())
                    .addChildBindingPoint(
                        d -> d
                            .name("targetId")
                            .override()
                            .provideValue(
                                typedObject(
                                    new TypeToken<List<QualifiedName>>() {},
                                    singletonList(new QualifiedName("name", namespace))))
                            .endNode())
                    .endNode())
            .addChildBindingPoint(
                d -> d
                    .name("targetId")
                    .input(IOBuilder::none)
                    .output(IOBuilder::none)
                    .model(listModel)
                    .override()
                    .concrete(false)
                    .addChildBindingPoint(e -> e.name("element").model(qualifiedNameModel))
                    .endNode())
            .addChildBindingPoint(
                d -> d
                    .name("data")
                    .input(
                        i -> i.provide(ImportReader.class).invoke(
                            "dereferenceImport",
                            i.bound("targetModel"),
                            i.bound("targetId"),
                            i.result()))
                    .output(
                        o -> o.provide(ImportWriter.class).invoke(
                            "referenceImport",
                            o.bound("targetModel"),
                            o.bound("targetId"),
                            o.source()))
                    .model(stringModel))
            .endNode());

    packageModel = modelFactory.apply(
        "package",
        t -> t
            .baseType(Package.class)
            .addChildBindingPoint(
                p -> p
                    .name("name")
                    .model(stringModel)
                    .input(i -> invokeStatic(Package.class, "getPackage"))
                    .output(o -> o.source().invoke("getName")))
            .endNode());

    typeModel = modelFactory.apply(
        "type",
        t -> t
            .baseType(Type.class)
            .addChildBindingPoint(
                p -> p
                    .name("name")
                    .model(stringModel)
                    .input(i -> invokeStatic(Types.class, "fromString", i.result()))
                    .output(o -> invokeStatic(Types.class, "toString", o.source())))
            .endNode());

    classModel = modelFactory
        .apply("class", t -> t.baseModel(new TypeToken<Class<?>>() {}, typeModel).endNode());

    annotatedTypeModel = modelFactory.apply(
        "annotatedType",
        t -> t
            .baseType(AnnotatedType.class)
            .addChildBindingPoint(
                p -> p
                    .name("name")
                    .model(stringModel)
                    .input(i -> invokeStatic(AnnotatedTypes.class, "fromString", i.result()))
                    .output(o -> invokeStatic(AnnotatedTypes.class, "toString", o.source())))
            .endNode());

    typeTokenModel = modelFactory.apply(
        "typeToken",
        t -> t
            .baseType(new TypeToken<TypeToken<?>>() {})
            .addChildBindingPoint(
                c -> c
                    .model(annotatedTypeModel)
                    .input(i -> invokeStatic(TypeToken.class, "overAnnotatedType", i.result()))
                    .output(o -> o.source().invoke("getAnnotatedDeclaration")))
            .endNode());

    enumModel = modelFactory.apply(
        "enum",
        t -> t
            .baseType(new TypeToken<Enum<?>>() {})
            .concrete(false)
            .addChildBindingPoint(
                c -> c
                    .name("enumType")
                    .input(IOBuilder::none)
                    .output(IOBuilder::none)
                    .type(new TypeToken<Class<? extends Enum<?>>>() {})
                    .input(
                        i -> i
                            .provide(ProcessingContext.class)
                            .invoke("getBindingNode")
                            .invoke("dataType")
                            .invoke("getRawType")))
            .addChildBindingPoint(
                p -> p
                    .name("name")
                    .input(
                        i -> Expressions.invokeStatic(
                            Enumeration.class,
                            "valueOfEnum",
                            i.bound("enumType"),
                            i.result()))
                    .output(o -> o.source().invoke("getName"))
                    .model(stringModel))
            .endNode());

    enumerationModel = modelFactory.apply(
        "enumeration",
        t -> t
            .baseType(new TypeToken<Enumeration<?>>() {})
            .concrete(false)
            .addChildBindingPoint(
                c -> c
                    .name("enumType")
                    .input(IOBuilder::none)
                    .output(IOBuilder::none)
                    .type(new TypeToken<Class<? extends Enumeration<?>>>() {})
                    .input(
                        i -> i
                            .provide(ProcessingContext.class)
                            .invoke("getBindingNode")
                            .invoke("dataType")
                            .invoke("getRawType")))
            .addChildBindingPoint(
                p -> p
                    .name("name")
                    .input(
                        i -> invokeStatic(
                            Enumeration.class,
                            "valueOf",
                            i.bound("enumType"),
                            i.result()))
                    .output(o -> o.source().invoke("getName"))
                    .model(stringModel))
            .endNode());

    rangeModel = modelFactory.apply(
        "range",
        t -> t
            .baseType(new TypeToken<Interval<Integer>>() {})
            .addChildBindingPoint(
                p -> p
                    .name("string")
                    .input(i -> invokeStatic(Range.class, "parse", i.result()))
                    .output(o -> invokeStatic(Range.class, "compose", o.source()))
                    .model(stringModel))
            .endNode());

    mapModel = null;

    /*
     * Schema
     */
    baseSchema = schemaConfigurator.create();
  }

  @Override
  public Model<String> stringModel() {
    return stringModel;
  }

  @Override
  public Model<byte[]> binaryModel() {
    return binaryModel;
  }

  @Override
  public Model<BigInteger> integerModel() {
    return integerModel;
  }

  @Override
  public Model<BigDecimal> decimalModel() {
    return decimalModel;
  }

  @Override
  public Model<Integer> intModel() {
    return intModel;
  }

  @Override
  public Model<Long> longModel() {
    return longModel;
  }

  @Override
  public Model<Float> floatModel() {
    return floatModel;
  }

  @Override
  public Model<Double> doubleModel() {
    return doubleModel;
  }

  @Override
  public Model<Boolean> booleanModel() {
    return booleanModel;
  }

  @Override
  public Model<QualifiedName> qualifiedNameModel() {
    return qualifiedNameModel;
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
  public Model<Void> referenceIndexModel() {
    return referenceIndexModel;
  }

  @Override
  public Model<Object> importModel() {
    return importModel;
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
  public Stream<Schema> dependencies() {
    return baseSchema.dependencies();
  }

  @Override
  public Stream<Model<?>> models() {
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
