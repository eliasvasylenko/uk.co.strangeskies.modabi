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
import static uk.co.strangeskies.reflection.codegen.InvocationExpression.invokeResolvedStatic;

import java.util.Arrays;
import java.util.function.Function;

import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.Models;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.io.Primitive;
import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingPointConfigurator;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPointBuilder;
import uk.co.strangeskies.modabi.schema.IOBuilder;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelBuilder;
import uk.co.strangeskies.modabi.schema.NodeBuilder;
import uk.co.strangeskies.modabi.schema.bindingconditions.AndCondition;
import uk.co.strangeskies.modabi.schema.bindingconditions.AscendingSortCondition;
import uk.co.strangeskies.modabi.schema.bindingconditions.DescendingSortCondition;
import uk.co.strangeskies.modabi.schema.bindingconditions.ForbiddenCondition;
import uk.co.strangeskies.modabi.schema.bindingconditions.OccurrencesCondition;
import uk.co.strangeskies.modabi.schema.bindingconditions.OptionalCondition;
import uk.co.strangeskies.modabi.schema.bindingconditions.OrCondition;
import uk.co.strangeskies.modabi.schema.bindingconditions.RequiredCondition;
import uk.co.strangeskies.modabi.schema.bindingconditions.SynchronizedCondition;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypeToken.Infer;

public class MetaSchemaImpl implements MetaSchema {
  private interface ModelHelper {
    <T> Model<T> apply(String name, Function<ModelBuilder, Model<T>> type);
  }

  private final Schema metaSchema;
  private Model<Schema> schemaModel;
  private Model<Model<?>> metaModel;

  public MetaSchemaImpl(SchemaBuilder schema, BaseSchema base) {
    QualifiedName name = QUALIFIED_NAME;
    Namespace namespace = name.getNamespace();

    /*
     * Schema
     */
    schema = schema.qualifiedName(name).dependencies(Arrays.asList(base));

    /*
     * Models
     */
    buildModels(new ModelHelper() {
      @Override
      public <T> Model<T> apply(String name, Function<ModelBuilder, Model<T>> modelFunction) {
        return modelFunction.apply(schema.addModel().name(new QualifiedName(name, namespace)));
      }
    }, base, namespace);

    metaSchema = schema.create();
  }

  private void buildModels(ModelHelper factory, BaseSchema base, Namespace namespace) {
    /* Node Base Models */

    Model<BindingPointConfigurator<?>> bindingPointModelBase = factory.apply(
        "bindingPointBase",
        m -> m.dataType(new TypeToken<BindingPointConfigurator<?>>() {}).concrete(false).export(
            false));

    Model<ModelBuilder<?>> metaModelBase = factory.apply(
        "modelBase",
        m -> m
            .baseModel(bindingPointModelBase)
            .dataType(new TypeToken<ModelBuilder<?>>() {})
            .concrete(false)
            .export(false));

    Model<ChildBindingPointBuilder<?>> childBindingPointModelBase = factory.apply(
        "childBindingPointBase",
        m -> m
            .baseModel(bindingPointModelBase)
            .dataType(new TypeToken<ChildBindingPointBuilder<?>>() {})
            .concrete(false)
            .export(false));

    /* Binding Condition Models */

    Model<BindingCondition<?>> bindingConditionModel = factory.apply(
        "bindingCondition",
        m -> m.concrete(false).dataType(new @Infer TypeToken<BindingCondition<?>>() {}));

    Model<AndCondition<?>> andModel = factory.apply(
        "andCondition",
        m -> m
            .baseModel(bindingConditionModel)
            .dataType(new @Infer TypeToken<AndCondition<?>>() {})
            .node(
                n -> n.addChildBindingPoint(
                    c -> c
                        .name("conditions")
                        .input(i -> invokeResolvedStatic(AndCondition.class, "and", i.result()))
                        .baseModel(base.derived().setModel())
                        .node(
                            p -> p.addChildBindingPoint(
                                h -> h.name("element").baseModel(bindingConditionModel))))));

    Model<OrCondition<?>> orModel = factory.apply(
        "orCondition",
        m -> m
            .baseType(new @Infer TypeToken<OrCondition<?>>() {})
            .baseModel(bindingConditionModel)
            .addChildBindingPoint(
                c -> c
                    .name("conditions")
                    .input(
                        i -> invokeStatic(
                            forStaticMethod(
                                OrCondition.class.getMethod("or", BindingCondition.class)),
                            i.result()))
                    .override()
                    .baseModel(base.setModel())
                    .addChildBindingPoint(h -> h.name("element").baseModel(bindingConditionModel)))
            .endNode());

    Model<RequiredCondition<?>> requiredModel = factory.apply(
        "requiredCondition",
        m -> m
            .baseModel(bindingConditionModel)
            .dataType(new @Infer TypeToken<RequiredCondition<?>>() {})
            .node(
                n -> n.initializeInput(
                    i -> invokeResolvedStatic(RequiredCondition.class, "required"))));

    Model<ForbiddenCondition<?>> forbiddenModel = factory.apply(
        "forbiddenCondition",
        m -> m
            .baseModel(bindingConditionModel)
            .dataType(new @Infer TypeToken<ForbiddenCondition<?>>() {})
            .node(
                n -> n.initializeInput(
                    i -> invokeResolvedStatic(ForbiddenCondition.class, "forbidden"))));

    Model<OptionalCondition<?>> optionalModel = factory.apply(
        "optionalCondition",
        m -> m
            .baseModel(bindingConditionModel)
            .dataType(new @Infer TypeToken<OptionalCondition<?>>() {})
            .node(
                n -> n.initializeInput(
                    i -> invokeResolvedStatic(OptionalCondition.class, "optional"))));

    Model<AscendingSortCondition<?>> sortAscendingModel = factory.apply(
        "sortAscendingCondition",
        m -> m
            .baseModel(bindingConditionModel)
            .dataType(new @Infer TypeToken<AscendingSortCondition<?>>() {})
            .node(
                n -> n.initializeInput(
                    i -> invokeResolvedStatic(AscendingSortCondition.class, "ascending"))));

    Model<DescendingSortCondition<?>> sortDescendingModel = factory.apply(
        "sortDescendingCondition",
        m -> m
            .baseModel(bindingConditionModel)
            .dataType(new @Infer TypeToken<DescendingSortCondition<?>>() {})
            .node(
                n -> n.initializeInput(
                    i -> invokeResolvedStatic(DescendingSortCondition.class, "descending"))));

    Model<SynchronizedCondition<?>> synchronizedModel = factory.apply(
        "synchronizedCondition",
        m -> m
            .baseModel(bindingConditionModel)
            .dataType(new @Infer TypeToken<SynchronizedCondition<?>>() {})
            .node(
                n -> n.initializeInput(
                    i -> invokeResolvedStatic(SynchronizedCondition.class, "asynchronous"))));

    Model<OccurrencesCondition<?>> occurrencesModel = factory.apply(
        "occurrencesCondition",
        m -> m
            .baseModel(bindingConditionModel)
            .dataType(new @Infer TypeToken<OccurrencesCondition<?>>() {})
            .node(
                n -> n.addChildBindingPoint(
                    c -> c.name("range").baseModel(base.derived().rangeModel()).input(
                        i -> invokeResolvedStatic(
                            OccurrencesCondition.class,
                            "occurrences",
                            i.result())))));

    /* Node Models */

    Model<BindingPointConfigurator<?, ?>> bindingPointModel = factory.apply(
        "bindingPoint",
        m -> m
            .concrete(false)
            .baseModel(bindingPointModelBase)
            .dataType(new TypeToken<BindingPointConfigurator<?, ?>>() {})
            .node(
                n -> n
                    .addChildBindingPoint(
                        c -> c
                            .name("name")
                            .baseModel(base.primitive(Primitive.STRING))
                            .bindingCondition(optional()))
                    .addChildBindingPoint(
                        c -> c
                            .name("export")
                            .baseModel(base.primitive(Primitive.BOOLEAN))
                            .bindingCondition(optional()))
                    .addChildBindingPoint(
                        c -> c
                            .name("concrete")
                            .baseModel(base.primitive(Primitive.BOOLEAN))
                            .bindingCondition(optional()))
                    .addChildBindingPoint(
                        c -> c.name("baseModel").baseModel(metaModelBase).bindingCondition(
                            optional()))
                    .addChildBindingPoint(
                        c -> c
                            .name("dataType")
                            .baseModel(base.derived().typeTokenModel())
                            .bindingCondition(optional()))));

    Model<ModelBuilder<?>> metaModel = factory.apply(
        "model",
        m -> m.baseModel(asList(metaModelBase, bindingPointModel)).dataType(
            new @Infer TypeToken<ModelBuilder<?>>() {}));

    Model<ChildBindingPointBuilder<?>> childBindingPointModel = factory.apply(
        "childBindingPoint",
        m -> m
            .baseModel(bindingPointModel)
            .dataType(new TypeToken<ChildBindingPointBuilder<?>>() {})
            .node(
                n -> n
                    .addChildBindingPoint(
                        c -> c
                            .name("extensible")
                            .baseModel(base.primitive(Primitive.BOOLEAN))
                            .bindingCondition(optional()))
                    .addChildBindingPoint(
                        c -> c.name("condition").baseModel(bindingConditionModel).bindingCondition(
                            optional()))
                    .addChildBindingPoint(
                        c -> c
                            .name("valueResolution")
                            .baseModel(base.derived().enumModel())
                            .dataType(ValueResolution.class)
                            .bindingCondition(optional()))
                    .addChildBindingPoint(
                        c -> c
                            .name("value")
                            .baseModel(base.derived().bufferedDataModel())
                            .input(i -> i.target().invokeResolvedMethod("provideValue", i.result()))
                            .output(o -> o.source().invokeResolvedMethod("getProvidedValue"))
                            .bindingCondition(optional()))));

    Model<NodeBuilder> nodeModel = factory.apply(
        "node",
        m -> m.concrete(false).dataType(new TypeToken<NodeBuilder>() {}).node(
            n -> n
                .addChildBindingPoint(
                    b -> b
                        .name("concrete")
                        .baseModel(base.primitive(Primitive.BOOLEAN))
                        .bindingCondition(optional()))
                .addChildBindingPoint(
                    b -> b
                        .name("child")
                        .extensible(true)
                        .concrete(false)
                        .dataType(new TypeToken<ChildBindingPoint<?>>() {})
                        .output(IOBuilder::none)
                        .bindingCondition(asynchronous().and(occurrences(between(0, null)))))
                .addChildBindingPoint(
                    b -> b.name("create").output(IOBuilder::none).input(
                        i -> i.target().assign(i.target().invokeResolvedMethod("create"))))));

    /*
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * TODO replace dataType(String) and getDataTypeString() methods with their
     * TypeToken equivalents, have a magic "node reference" to "imports" to support
     * unqualified names
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
  }

  @Override
  public QualifiedName qualifiedName() {
    return metaSchema.qualifiedName();
  }

  @Override
  public Schemata dependencies() {
    return metaSchema.dependencies();
  }

  @Override
  public Models models() {
    return metaSchema.models();
  }

  @Override
  public Model<Schema> getSchemaModel() {
    return schemaModel;
  }

  @Override
  public Model<Model<?>> getMetaModel() {
    return metaModel;
  }

  @Override
  public boolean equals(Object obj) {
    return metaSchema.equals(obj);
  }

  @Override
  public int hashCode() {
    return metaSchema.hashCode();
  }

  @Override
  public Imports imports() {
    return Imports.empty().withImport(Schema.class);
  }

  @Override
  public String toString() {
    return qualifiedName().toString();
  }
}
