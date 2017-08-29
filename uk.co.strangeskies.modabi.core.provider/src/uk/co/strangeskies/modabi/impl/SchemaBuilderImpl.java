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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.impl.schema.ModelBuilderImpl;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelBuilder;
import uk.co.strangeskies.modabi.schema.ModelFactory;
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

  private Map<String, Function<ModelBuilder, ModelFactory<?>>> pendingModelConfigurations;

  private Property<Schema> schemaProperty;
  private Schema schemaProxy;

  public SchemaBuilderImpl() {
    modelSet = new LinkedHashSet<>();
    dependencySet = new LinkedHashSet<>();
    imports = Imports.empty(Thread.currentThread().getContextClassLoader());

    pendingModelConfigurations = new HashMap<>();

    schemaProperty = new IdentityProperty<>();
    schemaProxy = (Schema) Proxy.newProxyInstance(
        Schema.class.getClassLoader(),
        new Class<?>[] { Schema.class },
        new InvocationHandler() {
          private Property<Schema> object = schemaProperty;

          @Override
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return method.invoke(object.get(), args);
          }
        });
  }

  public SchemaBuilderImpl(
      ModelBuilder modelBuilder,
      QualifiedName qualifiedName,
      Set<Model<?>> modelSet,
      Set<Schema> dependencySet,
      Imports imports,
      Map<String, Function<ModelBuilder, ModelFactory<?>>> pendingModelConfigurations,
      Property<Schema> schemaProperty,
      Schema schemaProxy) {
    this.qualifiedName = qualifiedName;
    this.modelSet = modelSet;
    this.dependencySet = dependencySet;
    this.imports = imports;
    this.pendingModelConfigurations = pendingModelConfigurations;
    this.schemaProxy = schemaProxy;
  }

  @Override
  public Schema create() {
    for (String pendingModel : pendingModelConfigurations.keySet()) {
      addModel(
          new QualifiedName(pendingModel, qualifiedName.getNamespace()),
          pendingModelConfigurations.get(pendingModel));
    }

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
      public Imports imports() {
        return imports;
      }

      @Override
      public boolean equals(Object obj) {
        if (!(obj instanceof Schema))
          return false;

        if (obj == this)
          return true;

        Schema other = (Schema) obj;

        return qualifiedName().equals(other.qualifiedName()) && models().equals(other.models())
            && dependencies().equals(other.dependencies()) && imports().equals(other.imports());
      }

      @Override
      public int hashCode() {
        return Objects.hash(qualifiedName(), models(), dependencies(), imports());
      }

      @Override
      public String toString() {
        return qualifiedName().toString();
      }
    });
    return schemaProperty.get();
  }

  @Override
  public SchemaBuilder qualifiedName(QualifiedName name) {
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
  public ModelConfiguratorDecorator addModel() {
    ModelBuilder configurator = new ModelBuilderImpl<>(schemaProxy);
    return () -> configurator;
  }

  @Override
  public SchemaBuilder addModel(
      String name,
      Function<ModelBuilder, ModelFactory<?>> configuration) {
    if (qualifiedName == null) {
      pendingModelConfigurations.put(name, configuration);
    } else {
      addModel(new QualifiedName(name, qualifiedName.getNamespace()), configuration);
    }
    return null;
  }

  @Override
  public <T> Model<T> generateModel(TypeToken<T> type) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }
}
