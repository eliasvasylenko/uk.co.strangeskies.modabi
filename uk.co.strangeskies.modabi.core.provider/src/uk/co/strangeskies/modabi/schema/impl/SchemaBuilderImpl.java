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

import static uk.co.strangeskies.collection.stream.StreamUtilities.upcastStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.expression.functional.FunctionalExpressionCompiler;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.Models;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.meta.ModelBuilder;
import uk.co.strangeskies.modabi.schema.meta.SchemaBuilder;
import uk.co.strangeskies.property.Property;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.token.TypeToken;

@Component
public class SchemaBuilderImpl implements SchemaBuilder {
  private final QualifiedName name;
  private final Map<ModelBuilderImpl, ModelImpl<?>> models;
  private final Set<Schema> dependencies;

  // TODO constructor injection
  @Reference
  private FunctionalExpressionCompiler expressionCompiler;

  public SchemaBuilderImpl(FunctionalExpressionCompiler expressionCompiler) {
    this.expressionCompiler = expressionCompiler;

    name = null;
    models = new LinkedHashMap<>();
    dependencies = new LinkedHashSet<>();
  }

  SchemaBuilderImpl() {
    name = null;
    models = new LinkedHashMap<>();
    dependencies = new LinkedHashSet<>();
  }

  private SchemaBuilderImpl(
      FunctionalExpressionCompiler expressionCompiler,
      QualifiedName qualifiedName,
      Map<ModelBuilderImpl, ModelImpl<?>> models,
      Set<Schema> dependencies) {
    this.expressionCompiler = expressionCompiler;
    this.name = qualifiedName;
    this.models = models;
    this.dependencies = dependencies;
  }

  public SchemaBuilderImpl(
      ModelBuilder modelBuilder,
      QualifiedName qualifiedName,
      Map<ModelBuilderImpl, ModelImpl<?>> models,
      Set<Schema> dependencies,
      Imports imports,
      Property<Schema> schemaProperty) {
    this.name = qualifiedName;
    this.models = models;
    this.dependencies = dependencies;
  }

  @Override
  public Schema create() {
    final QualifiedName qualifiedName = this.name;
    final Models models = new Models(this.models.values());
    final List<Schema> dependencies = new ArrayList<>(this.dependencies);

    return new Schema() {
      @Override
      public QualifiedName name() {
        return qualifiedName;
      }

      @Override
      public Models models() {
        return models;
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
    return new SchemaBuilderImpl(expressionCompiler, name, models, dependencies);
  }

  @Override
  public Optional<QualifiedName> getName() {
    return Optional.ofNullable(name);
  }

  @Override
  public SchemaBuilder dependencies(Collection<? extends Schema> dependencies) {
    return new SchemaBuilderImpl(
        expressionCompiler,
        name,
        models,
        new LinkedHashSet<>(dependencies));
  }

  @Override
  public Stream<Schema> getDependencies() {
    return dependencies.stream();
  }

  @Override
  public ModelBuilder.NameStep addModel() {
    return new ModelBuilderImpl(this);
  }

  @Override
  public <T> Model<T> generateModel(TypeToken<T> type) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  public SchemaBuilder endModel(ModelBuilderImpl modelBuilder) {
    Map<ModelBuilderImpl, ModelImpl<?>> models = new LinkedHashMap<>(this.models);
    models.put(modelBuilder, new ModelImpl<>(modelBuilder));
    return new SchemaBuilderImpl(expressionCompiler, name, models, dependencies);
  }

  @Override
  public Stream<ModelBuilder> getModels() {
    return upcastStream(models.keySet().stream());
  }

  protected FunctionalExpressionCompiler getExpressionCompiler() {
    return expressionCompiler;
  }

  protected Model<?> getModel(QualifiedName baseModel) {
    // TODO Auto-generated method stub
    return null;
  }
}
