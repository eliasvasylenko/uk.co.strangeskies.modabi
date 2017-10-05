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

import static java.util.Collections.singleton;
import static uk.co.strangeskies.mathematics.Interval.leftBounded;
import static uk.co.strangeskies.modabi.schema.BindingConditionPrototype.allOf;
import static uk.co.strangeskies.modabi.schema.BindingConditionPrototype.occurrences;
import static uk.co.strangeskies.modabi.schema.BindingConditionPrototype.optional;
import static uk.co.strangeskies.modabi.schema.BindingConditionPrototype.synchronous;
import static uk.co.strangeskies.modabi.schema.expression.Expressions.invokeStatic;

import java.util.function.Function;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.schema.BindingConditionPrototype;
import uk.co.strangeskies.modabi.schema.ChildBindingPointBuilder;
import uk.co.strangeskies.modabi.schema.IOBuilder;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelBuilder;
import uk.co.strangeskies.modabi.schema.NodeBuilder;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypeToken.Infer;
import uk.co.strangeskies.utility.IdentityProperty;
import uk.co.strangeskies.utility.Property;

public class MetaSchemaImpl implements MetaSchema {
  private interface ModelHelper {
    <T> Model<T> apply(String name, Function<ModelBuilder<?>, ModelBuilder<T>> type);
  }

  private final Schema metaSchema;
  private Model<Schema> schemaModel;
  private Model<Model<?>> metaModel;

  public MetaSchemaImpl(SchemaBuilder schemaBuilder, BaseSchema base) {
    QualifiedName name = QUALIFIED_NAME;
    Namespace namespace = name.getNamespace();

    /*
     * Schema
     */
    Property<SchemaBuilder> schemaConfigurator = new IdentityProperty<>(
        schemaBuilder.name(name).dependencies(singleton(base)));

    /*
     * Models
     */
    ModelHelper modelFactory = new ModelHelper() {
      @Override
      public <T> Model<T> apply(String name, Function<ModelBuilder<?>, ModelBuilder<T>> type) {
        Property<Model<T>> completion = new IdentityProperty<>();
        schemaConfigurator.set(
            type
                .apply(schemaConfigurator.get().addModel().name(new QualifiedName(name, namespace)))
                .endModel(completion::set));
        return completion.get();
      }
    };

    metaSchema = schemaConfigurator.get().create();

    /* Binding Condition Models */

    Model<BindingConditionPrototype> bindingConditionModel = modelFactory.apply(
        "bindingCondition",
        m -> m
            .rootNode(new @Infer TypeToken<BindingConditionPrototype>() {})
            .extensible(true)
            .concrete(false)
            .endNode());

    Model<BindingConditionPrototype> allOfModel = modelFactory.apply(
        "andCondition",
        m -> m
            .rootNode(bindingConditionModel)
            .addChildBindingPoint(
                c -> c
                    .name("conditions")
                    .input(i -> invokeStatic(BindingConditionPrototype.class, "allOf", i.result()))
                    .model(base.setModel())
                    .overrideNode()
                    .addChildBindingPoint(h -> h.name("element").model(bindingConditionModel))
                    .endNode())
            .endNode());

    Model<BindingConditionPrototype> anyOfModel = modelFactory.apply(
        "andCondition",
        m -> m
            .rootNode(bindingConditionModel)
            .addChildBindingPoint(
                c -> c
                    .name("conditions")
                    .input(i -> invokeStatic(BindingConditionPrototype.class, "anyOf", i.result()))
                    .model(base.setModel())
                    .overrideNode()
                    .addChildBindingPoint(h -> h.name("element").model(bindingConditionModel))
                    .endNode())
            .endNode());

    Model<BindingConditionPrototype> requiredModel = modelFactory.apply(
        "requiredCondition",
        m -> m
            .rootNode(bindingConditionModel)
            .initializeInput(i -> invokeStatic(BindingConditionPrototype.class, "required"))
            .endNode());

    Model<BindingConditionPrototype> forbiddenModel = modelFactory.apply(
        "forbiddenCondition",
        m -> m
            .rootNode(bindingConditionModel)
            .initializeInput(i -> invokeStatic(BindingConditionPrototype.class, "forbidden"))
            .endNode());

    Model<BindingConditionPrototype> optionalModel = modelFactory.apply(
        "optionalCondition",
        m -> m
            .rootNode(bindingConditionModel)
            .initializeInput(i -> invokeStatic(BindingConditionPrototype.class, "optional"))
            .endNode());

    Model<BindingConditionPrototype> sortAscendingModel = modelFactory.apply(
        "sortAscendingCondition",
        m -> m
            .rootNode(bindingConditionModel)
            .initializeInput(i -> invokeStatic(BindingConditionPrototype.class, "ascending"))
            .endNode());

    Model<BindingConditionPrototype> sortDescendingModel = modelFactory.apply(
        "sortDescendingCondition",
        m -> m
            .rootNode(bindingConditionModel)
            .initializeInput(i -> invokeStatic(BindingConditionPrototype.class, "descending"))
            .endNode());

    Model<BindingConditionPrototype> synchronizedModel = modelFactory.apply(
        "synchronizedCondition",
        m -> m
            .rootNode(bindingConditionModel)
            .initializeInput(i -> invokeStatic(BindingConditionPrototype.class, "synchronous"))
            .endNode());

    Model<BindingConditionPrototype> occurrencesModel = modelFactory.apply(
        "occurrencesCondition",
        m -> m
            .rootNode(bindingConditionModel)
            .addChildBindingPoint(
                c -> c.name("range").model(base.rangeModel()).input(
                    i -> invokeStatic(BindingConditionPrototype.class, "occurrences", i.result())))
            .endNode());

    /* Node Models */

    Model<ModelBuilder<?>> metaModelBase = modelFactory.apply(
        "metaModelBase",
        m -> m
            .export(false)
            .rootNode(new TypeToken<ModelBuilder<?>>() {})
            .concrete(false)
            .endNode());

    Model<ChildBindingPointBuilder<?>> bindingPointModel = modelFactory.apply(
        "bindingPoint",
        m -> m
            .rootNode(new TypeToken<ChildBindingPointBuilder<?>>() {})
            .addChildBindingPoint(
                c -> c.name("name").model(base.stringModel()).bindingCondition(optional()))
            .addChildBindingPoint(
                c -> c.name("export").model(base.booleanModel()).bindingCondition(optional()))
            .addChildBindingPoint(
                c -> c.name("concrete").model(base.booleanModel()).bindingCondition(optional()))
            .addChildBindingPoint(
                c -> c.name("baseModel").model(metaModelBase).bindingCondition(optional()))
            .addChildBindingPoint(
                c -> c.name("dataType").model(base.typeTokenModel()).bindingCondition(optional()))
            .addChildBindingPoint(
                c -> c.name("extensible").model(base.booleanModel()).bindingCondition(optional()))
            .addChildBindingPoint(
                c -> c.name("condition").model(bindingConditionModel).bindingCondition(optional()))
            .addChildBindingPoint(
                c -> c
                    .name("value")
                    // TODO .rootNode(base.bufferedDataModel())
                    .input(i -> i.target().invoke("provideValue", i.result()))
                    .output(o -> o.source().invoke("getProvidedValue"))
                    .bindingCondition(optional()))
            .endNode());

    Model<NodeBuilder<?>> nodeModel = modelFactory.apply(
        "node",
        m -> m
            .rootNode(new TypeToken<NodeBuilder<?>>() {})
            .addChildBindingPoint(
                b -> b.name("concrete").model(base.booleanModel()).bindingCondition(optional()))
            .addChildBindingPoint(
                b -> b
                    .name("child")
                    .model(bindingPointModel)
                    .output(IOBuilder::none)
                    .bindingCondition(allOf(synchronous(), occurrences(leftBounded(0)))))
            .addChildBindingPoint(
                b -> b.name("create").output(IOBuilder::none).input(
                    i -> i.target().assign(i.target().invoke("create"))))
            .endNode());

    Model<ModelBuilder<?>> metaModel = modelFactory
        .apply("model", m -> m.rootNode(metaModelBase).endNode());

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
  public Stream<Schema> dependencies() {
    return metaSchema.dependencies();
  }

  @Override
  public Stream<Model<?>> models() {
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
  public String toString() {
    return qualifiedName().toString();
  }
}
