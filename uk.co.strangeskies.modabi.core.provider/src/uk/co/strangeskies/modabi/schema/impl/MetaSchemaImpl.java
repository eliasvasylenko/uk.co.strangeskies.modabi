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

import static uk.co.strangeskies.modabi.expression.Expressions.invokeStatic;
import static uk.co.strangeskies.modabi.schema.BaseSchema.BOOLEAN_MODEL;
import static uk.co.strangeskies.modabi.schema.BaseSchema.INTERVAL_MODEL;
import static uk.co.strangeskies.modabi.schema.BaseSchema.QUALIFIED_NAME_MODEL;
import static uk.co.strangeskies.modabi.schema.BaseSchema.SET_MODEL;
import static uk.co.strangeskies.modabi.schema.BaseSchema.STRING_MODEL;
import static uk.co.strangeskies.modabi.schema.BaseSchema.TYPE_TOKEN_MODEL;
import static uk.co.strangeskies.modabi.schema.BindingConstraintSpecification.optional;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.boundValue;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.object;

import java.util.stream.Stream;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.BindingConstraintSpecification;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.Models;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.meta.ChildBuilder;
import uk.co.strangeskies.modabi.schema.meta.MetaSchema;
import uk.co.strangeskies.modabi.schema.meta.ModelBuilder;
import uk.co.strangeskies.modabi.schema.meta.SchemaBuilder;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypeToken.Infer;

public class MetaSchemaImpl implements MetaSchema {
  private final Schema metaSchema;

  public MetaSchemaImpl(SchemaBuilder schemaBuilder) {
    schemaBuilder = schemaBuilder.name(META_SCHEMA);

    schemaBuilder = schemaBuilder
        .addModel()
        .name(BINDING_CONDITION_MODEL)
        .type(new @Infer TypeToken<BindingConstraintSpecification>() {})
        .partial()
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(AND_CONDITION_MODEL)
        .baseModel(BINDING_CONDITION_MODEL)
        .addChild(
            c -> c
                .name("conditions")
                .input(invokeStatic(BindingConstraintSpecification.class, "allOf", boundValue()))
                .model(SET_MODEL)
                .overrideModel()
                .addChild(h -> h.name("element").model(BINDING_CONDITION_MODEL))
                .endOverride())
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(OR_CONDITION_MODEL)
        .baseModel(BINDING_CONDITION_MODEL)
        .addChild(
            c -> c
                .name("conditions")
                .input(invokeStatic(BindingConstraintSpecification.class, "anyOf", boundValue()))
                .model(SET_MODEL)
                .overrideModel()
                .addChild(h -> h.name("element").model(BINDING_CONDITION_MODEL))
                .endOverride())
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name("requiredCondition")
        .baseModel(BINDING_CONDITION_MODEL)
        .addChild()
        .input(invokeStatic(BindingConstraintSpecification.class, "required"))
        .endChild()
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name("forbiddenCondition")
        .baseModel(BINDING_CONDITION_MODEL)
        .addChild()
        .input(invokeStatic(BindingConstraintSpecification.class, "forbidden"))
        .endChild()
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name("optionalCondition")
        .baseModel(BINDING_CONDITION_MODEL)
        .addChild()
        .input(invokeStatic(BindingConstraintSpecification.class, "optional"))
        .endChild()
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name("sortAscendingCondition")
        .baseModel(BINDING_CONDITION_MODEL)
        .addChild()
        .input(invokeStatic(BindingConstraintSpecification.class, "ascending"))
        .endChild()
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name("sortDescendingCondition")
        .baseModel(BINDING_CONDITION_MODEL)
        .addChild()
        .input(invokeStatic(BindingConstraintSpecification.class, "descending"))
        .endChild()
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name("synchronizedCondition")
        .baseModel(BINDING_CONDITION_MODEL)
        .addChild()
        .input(invokeStatic(BindingConstraintSpecification.class, "synchronous"))
        .endChild()
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name("occurrencesCondition")
        .baseModel(BINDING_CONDITION_MODEL)
        .addChild(
            c -> c
                .name("range")
                .model(INTERVAL_MODEL)
                .input(invokeStatic(BindingConstraintSpecification.class, "occurrences", boundValue())))
        .endModel();

    /* Node Models */

    schemaBuilder = schemaBuilder
        .addModel()
        .name(CHILD_BUILDER_MODEL)
        .type(new TypeToken<ChildBuilder<?>>() {})
        .addChild(c -> c.name("name").model(STRING_MODEL).bindingConstraint(optional()))
        .addChild(c -> c.name("export").model(BOOLEAN_MODEL).bindingConstraint(optional()))
        .addChild(c -> c.name("concrete").model(BOOLEAN_MODEL).bindingConstraint(optional()))
        .addChild(c -> c.name("baseModel").model(QUALIFIED_NAME_MODEL).bindingConstraint(optional()))
        .addChild(c -> c.name("dataType").model(TYPE_TOKEN_MODEL).bindingConstraint(optional()))
        .addChild(c -> c.name("extensible").model(BOOLEAN_MODEL).bindingConstraint(optional()))
        .addChild(
            c -> c.name("condition").model(BINDING_CONDITION_MODEL).bindingConstraint(optional()))
        .addChild(
            c -> c
                .name("value")
                // TODO .rootNode(base.bufferedDataModel())
                .input(object().invoke("provideValue", boundValue()))
                .output(object().invoke("getProvidedValue"))
                .bindingConstraint(optional()))
        .endModel();

    schemaBuilder = schemaBuilder
        .addModel()
        .name(MODEL_BUILDER_MODEL)
        .type(new TypeToken<ModelBuilder>() {})
        .partial()
        .endModel();

    metaSchema = schemaBuilder.create();
  }

  @SuppressWarnings("unchecked")
  private <T> Model<T> getModel(QualifiedName name) {
    return (Model<T>) metaSchema.models().get(name);
  }

  @Override
  public Model<Schema> getSchemaModel() {
    return getModel(SCHEMA_MODEL);
  }

  @Override
  public Model<SchemaBuilder> getSchemaBuilderModel() {
    return getModel(SCHEMA_BUILDER_MODEL);
  }

  /* Schema */

  @Override
  public QualifiedName name() {
    return metaSchema.name();
  }

  @Override
  public Stream<Schema> dependencies() {
    return metaSchema.dependencies();
  }

  @Override
  public Models models() {
    return metaSchema.models();
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
    return name().toString();
  }
}
