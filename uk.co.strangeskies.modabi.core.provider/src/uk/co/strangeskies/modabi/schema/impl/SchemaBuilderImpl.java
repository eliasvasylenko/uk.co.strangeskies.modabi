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

import static java.lang.Thread.currentThread;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.expression.functional.FunctionalExpressionCompiler;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.meta.ModelBuilder;
import uk.co.strangeskies.modabi.schema.meta.SchemaBuilder;
import uk.co.strangeskies.property.Property;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.token.TypeToken;

@Component
public class SchemaBuilderImpl implements SchemaBuilder {
  private QualifiedName qualifiedName;
  private final Set<Model<?>> modelSet;
  private final Set<Schema> dependencySet;
  private Imports imports;

  // TODO constructor injection
  @Reference
  private FunctionalExpressionCompiler expressionCompiler;

  public SchemaBuilderImpl(FunctionalExpressionCompiler expressionCompiler) {
    this.expressionCompiler = expressionCompiler;

    modelSet = new LinkedHashSet<>();
    dependencySet = new LinkedHashSet<>();
    imports = Imports.empty(Thread.currentThread().getContextClassLoader());
  }

  SchemaBuilderImpl() {
    modelSet = new LinkedHashSet<>();
    dependencySet = new LinkedHashSet<>();
    imports = Imports.empty(Thread.currentThread().getContextClassLoader());
  }

  public SchemaBuilderImpl(
      ModelBuilder<?> modelBuilder,
      QualifiedName qualifiedName,
      Set<Model<?>> modelSet,
      Set<Schema> dependencySet,
      Imports imports,
      Property<Schema> schemaProperty) {
    this.qualifiedName = qualifiedName;
    this.modelSet = modelSet;
    this.dependencySet = dependencySet;
  }

  @Override
  public Schema create() {
    final QualifiedName qualifiedName = this.qualifiedName;
    final List<Model<?>> models = new ArrayList<>(modelSet);
    final List<Schema> dependencies = new ArrayList<>(dependencySet);

    return new Schema() {
      @Override
      public QualifiedName name() {
        return qualifiedName;
      }

      @Override
      public Stream<Model<?>> models() {
        return models.stream();
      }

      @Override
      public Stream<Schema> dependencies() {
        return dependencies.stream();
      }

      @Override
      public boolean equals(Object obj) {
        if (!(obj instanceof Schema))
          return false;

        if (obj == this)
          return true;

        Schema other = (Schema) obj;

        return name().equals(other.name()) && models().equals(other.models())
            && dependencies().equals(other.dependencies());
      }

      @Override
      public int hashCode() {
        return Objects.hash(name(), models(), dependencies());
      }

      @Override
      public String toString() {
        return name().toString();
      }
    };
  }

  @Override
  public SchemaBuilder name(QualifiedName name) {
    qualifiedName = name;

    return this;
  }

  @Override
  public SchemaBuilder dependencies(Collection<? extends Schema> dependencies) {
    dependencySet.clear();
    dependencySet.addAll(dependencies);

    return this;
  }

  @Override
  public SchemaBuilder imports(Collection<? extends Class<?>> imports) {
    this.imports = Imports.empty(currentThread().getContextClassLoader()).withImports(imports);

    return this;
  }

  @Override
  public ModelBuilder<?> addModel() {
    ModelBuilder<?> configurator = new ModelBuilderImpl<>(this);
    return configurator;
  }

  @Override
  public <T> Model<T> generateModel(TypeToken<T> type) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  public SchemaBuilder endModel(ModelImpl<?> model) {
    modelSet.add(model);
    return this;
  }

  protected FunctionalExpressionCompiler getExpressionCompiler() {
    return expressionCompiler;
  }
}
