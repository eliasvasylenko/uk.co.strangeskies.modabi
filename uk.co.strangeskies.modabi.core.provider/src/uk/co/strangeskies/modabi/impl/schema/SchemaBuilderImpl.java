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
package uk.co.strangeskies.modabi.impl.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelBuilder;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.utility.IdentityProperty;
import uk.co.strangeskies.utility.Property;

@Component
public class SchemaBuilderImpl implements SchemaBuilder {
  private QualifiedName qualifiedName;
  private final Set<Model<?>> modelSet;
  private final Set<Schema> dependencySet;
  private Imports imports;

  private Property<Schema> schemaProperty;

  public SchemaBuilderImpl() {
    modelSet = new LinkedHashSet<>();
    dependencySet = new LinkedHashSet<>();
    imports = Imports.empty(Thread.currentThread().getContextClassLoader());

    schemaProperty = new IdentityProperty<>();
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
    final List<Model<?>> models = new ArrayList<>();
    models.addAll(modelSet);
    final List<Schema> dependencies = new ArrayList<>();
    dependencies.addAll(dependencySet);

    schemaProperty.set(new Schema() {
      @Override
      public QualifiedName qualifiedName() {
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

        return qualifiedName().equals(other.qualifiedName()) && models().equals(other.models())
            && dependencies().equals(other.dependencies());
      }

      @Override
      public int hashCode() {
        return Objects.hash(qualifiedName(), models(), dependencies());
      }

      @Override
      public String toString() {
        return qualifiedName().toString();
      }
    });
    return schemaProperty.get();
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
    this.imports = Imports.empty(Thread.currentThread().getContextClassLoader()).withImports(
        imports);

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
}
