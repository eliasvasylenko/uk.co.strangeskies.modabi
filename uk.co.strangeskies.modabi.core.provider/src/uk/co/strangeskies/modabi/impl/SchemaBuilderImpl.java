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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.osgi.service.component.annotations.Component;

import uk.co.strangeskies.modabi.Models;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.SchemaConfigurator;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.impl.schema.ModelBuilderImpl;
import uk.co.strangeskies.modabi.impl.schema.building.ModelConfiguratorDecorator;
import uk.co.strangeskies.modabi.schema.DataLoader;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelBuilder;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Property;

@Component
public class SchemaBuilderImpl implements SchemaBuilder {
	public class SchemaConfiguratorImpl implements SchemaConfigurator {
		class ModelConfiguratorDecoratorImpl extends ModelConfiguratorDecorator {
			public ModelConfiguratorDecoratorImpl(ModelConfigurator component) {
				super(component);
			}

			@Override
			public Model<T> create() {
				Model<T> model = super.create();
				modelSet.add(model);
				return model;
			}
		}

		private final ModelBuilder modelBuilder;

		private final DataLoader loader;

		private QualifiedName qualifiedName;
		private final Set<Model<?>> modelSet;
		private final Schemata dependencySet;
		private Imports imports;

		private Map<String, Function<ModelConfigurator<?>, ModelConfigurator<?>>> pendingModelConfigurations;

		private Property<Schema, Schema> schemaProperty;
		private Schema schemaProxy;

		public SchemaConfiguratorImpl(DataLoader loader) {
			this(loader, new ModelBuilderImpl());
		}

		public SchemaConfiguratorImpl(DataLoader loader, ModelBuilder modelBuilder) {
			this.modelBuilder = modelBuilder;
			this.loader = loader;

			modelSet = new LinkedHashSet<>();
			dependencySet = new Schemata();
			imports = Imports.empty(Thread.currentThread().getContextClassLoader());

			pendingModelConfigurations = new HashMap<>();

			schemaProperty = new IdentityProperty<>();
			schemaProxy = (Schema) Proxy.newProxyInstance(
					Schema.class.getClassLoader(),
					new Class<?>[] { Schema.class },
					new InvocationHandler() {
						private Property<Schema, Schema> object = schemaProperty;

						@Override
						public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
							return method.invoke(object.get(), args);
						}
					});
		}

		@Override
		public Schema create() {
			for (String pendingModel : pendingModelConfigurations.keySet()) {
				addModel(
						new QualifiedName(pendingModel, qualifiedName.getNamespace()),
						pendingModelConfigurations.get(pendingModel));
			}

			final QualifiedName qualifiedName = this.qualifiedName;
			final Models models = new Models();
			models.addAll(modelSet);
			final Schemata dependencies = new Schemata();
			dependencies.addAll(dependencySet);

			schemaProperty.set(new Schema() {
				@Override
				public QualifiedName qualifiedName() {
					return qualifiedName;
				}

				@Override
				public Models models() {
					return models;
				}

				@Override
				public Schemata dependencies() {
					return dependencies;
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

					return qualifiedName().equals(other.qualifiedName())
							&& models().equals(other.models())
							&& dependencies().equals(other.dependencies())
							&& imports().equals(other.imports());
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
		public SchemaConfigurator qualifiedName(QualifiedName name) {
			qualifiedName = name;

			return this;
		}

		@Override
		public SchemaConfigurator dependencies(Collection<? extends Schema> dependencies) {
			dependencySet.clear();
			dependencySet.addAll(dependencies);

			return this;
		}

		@Override
		public SchemaConfigurator imports(Collection<? extends Class<?>> imports) {
			this.imports = Imports.empty(Thread.currentThread().getContextClassLoader()).withImports(
					imports);

			return this;
		}

		@Override
		public ModelConfigurator addModel() {
			return new ModelConfiguratorDecoratorImpl(
					modelBuilder.configure(loader, schemaProxy, imports));
		}

		@Override
		public SchemaConfigurator addModel(
				String name,
				Function<ModelConfigurator<?>, ModelConfigurator<?>> configuration) {
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

	@Override
	public SchemaConfigurator configure(DataLoader loader) {
		return new SchemaConfiguratorImpl(loader);
	}
}
