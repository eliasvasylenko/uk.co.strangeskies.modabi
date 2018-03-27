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
package uk.co.strangeskies.modabi.model.building.test;

import static uk.co.strangeskies.modabi.schema.BindingExpressions.boundValue;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.object;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.LOOSE_COMPATIBILILTY;

import java.util.Set;
import java.util.SortedSet;

import org.junit.Test;

import mockit.Injectable;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.binding.impl.BindingServiceImpl;
import uk.co.strangeskies.modabi.expression.impl.FunctionalExpressionCompilerImpl;
import uk.co.strangeskies.modabi.io.StructuredDataWriter;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.Schemata;
import uk.co.strangeskies.modabi.schema.impl.CoreSchemata;
import uk.co.strangeskies.modabi.schema.impl.SchemaBuilderImpl;
import uk.co.strangeskies.modabi.schema.meta.SchemaBuilder;
import uk.co.strangeskies.property.IdentityProperty;
import uk.co.strangeskies.property.Property;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypeToken.Infer;

public class SchemaBuildingTests {
  @Injectable
  private StructuredDataWriter writer;

  @Test
  public void buildBaseSchemataTest() {
    SchemaBuilder builder = new SchemaBuilderImpl(new FunctionalExpressionCompilerImpl());
    new CoreSchemata(() -> builder);
  }

  @Test
  public void bindOutBaseSchemaTest() {
    Property<Model<String>> daftModel = new IdentityProperty<>();

    SchemaBuilder builder = new SchemaBuilderImpl(new FunctionalExpressionCompilerImpl());
    CoreSchemata coreSchemata = new CoreSchemata(() -> builder);
    Schemata schemata = new Schemata(coreSchemata.baseSchema());
    schemata.add(coreSchemata.metaSchema());

    Schema schema = new SchemaBuilderImpl(new FunctionalExpressionCompilerImpl())
        .name(new QualifiedName("SillyBillies"))
        .addModel()
        .name(new QualifiedName("daft"))
        .type(String.class)
        .addChild(
            c -> c
                .name("kid")
                .model(schemata.getBaseSchema().stringModel().name())
                .input(object().assign(boundValue()))
                .output(object()))
        .endModel()
        .create();

    schemata.add(schema);

    System.out
        .println(
            new BindingServiceImpl().bindOutput("test-string").from(daftModel.get()).to(writer));
  }

  @Test
  public void fixMeUp() {
    System.out
        .println(
            new @Infer TypeToken<SortedSet<?>>() {}
                .withConstraintTo(LOOSE_COMPATIBILILTY, new TypeToken<Set<String>>() {})
                .resolve());
    System.out
        .println(
            new TypeToken<@Infer SortedSet<?>>() {}
                .withConstraintTo(LOOSE_COMPATIBILILTY, new TypeToken<Set<String>>() {})
                .resolve());
    System.out
        .println(
            new TypeToken<SortedSet<@Infer ?>>() {}
                .withConstraintTo(LOOSE_COMPATIBILILTY, new TypeToken<Set<String>>() {})
                .resolve());
  }
}
