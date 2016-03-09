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
import java.util.Set;
import java.util.function.Function;

import org.osgi.service.component.annotations.Component;

import uk.co.strangeskies.modabi.DataTypes;
import uk.co.strangeskies.modabi.Models;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.SchemaConfigurator;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.impl.schema.building.DataTypeBuilderImpl;
import uk.co.strangeskies.modabi.impl.schema.building.DataTypeConfiguratorDecorator;
import uk.co.strangeskies.modabi.impl.schema.building.ModelBuilderImpl;
import uk.co.strangeskies.modabi.impl.schema.building.ModelConfiguratorDecorator;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.DataTypeConfigurator;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.modabi.schema.building.DataTypeBuilder;
import uk.co.strangeskies.modabi.schema.building.ModelBuilder;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Property;

@Component
public class SchemaBuilderImpl implements SchemaBuilder {
	public class SchemaConfiguratorImpl implements SchemaConfigurator {
		private final ModelBuilder modelBuilder;
		private final DataTypeBuilder dataTypeBuilder;

		private final DataLoader loader;

		private final Set<DataType<?>> typeSet;
		private QualifiedName qualifiedName;
		private final Set<Model<?>> modelSet;
		private final Schemata dependencySet;
		private Imports imports;

		private Map<String, Function<DataTypeConfigurator<Object>, DataTypeConfigurator<?>>> pendingDataTypeConfigurations;
		private Map<String, Function<ModelConfigurator<Object>, ModelConfigurator<?>>> pendingModelConfigurations;

		private Property<Schema, Schema> schemaProperty;
		private Schema schemaProxy;

		public SchemaConfiguratorImpl(DataLoader loader) {
			this(loader, new ModelBuilderImpl(), new DataTypeBuilderImpl());
		}

		public SchemaConfiguratorImpl(DataLoader loader, ModelBuilder modelBuilder, DataTypeBuilder dataTypeBuilder) {
			this.modelBuilder = modelBuilder;
			this.dataTypeBuilder = dataTypeBuilder;
			this.loader = loader;

			typeSet = new LinkedHashSet<>();
			modelSet = new LinkedHashSet<>();
			dependencySet = new Schemata();
			imports = Imports.empty();

			pendingDataTypeConfigurations = new HashMap<>();
			pendingModelConfigurations = new HashMap<>();

			schemaProperty = new IdentityProperty<>();
			schemaProxy = (Schema) Proxy.newProxyInstance(Schema.class.getClassLoader(), new Class<?>[] { Schema.class },
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
			for (String pendingDataType : pendingDataTypeConfigurations.keySet()) {
				addDataType(new QualifiedName(pendingDataType, qualifiedName.getNamespace()),
						pendingDataTypeConfigurations.get(pendingDataType));
			}
			for (String pendingModel : pendingModelConfigurations.keySet()) {
				addModel(new QualifiedName(pendingModel, qualifiedName.getNamespace()),
						pendingModelConfigurations.get(pendingModel));
			}

			final QualifiedName qualifiedName = this.qualifiedName;
			final DataTypes types = new DataTypes();
			types.addAll(typeSet);
			final Models models = new Models();
			models.addAll(modelSet);
			final Schemata dependencies = new Schemata();
			dependencies.addAll(dependencySet);

			schemaProperty.set(new Schema() {
				@Override
				public DataTypes getDataTypes() {
					return types;
				}

				@Override
				public QualifiedName getQualifiedName() {
					return qualifiedName;
				}

				@Override
				public Models getModels() {
					return models;
				}

				@Override
				public Schemata getDependencies() {
					return dependencies;
				}

				@Override
				public Imports getImports() {
					return imports;
				}

				@Override
				public boolean equals(Object obj) {
					if (!(obj instanceof Schema))
						return false;

					if (obj == this)
						return true;

					Schema other = (Schema) obj;

					return getQualifiedName().equals(other.getQualifiedName()) && getModels().equals(other.getModels())
							&& getDataTypes().equals(other.getDataTypes()) && getDependencies().equals(other.getDependencies())
							&& getImports().equals(other.getImports());
				}

				@Override
				public int hashCode() {
					return getDataTypes().hashCode() ^ getQualifiedName().hashCode() ^ getModels().hashCode()
							^ getDependencies().hashCode() ^ getImports().hashCode();
				}

				@Override
				public String toString() {
					return getQualifiedName().toString();
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
			this.imports = Imports.empty().withImports(imports);

			return this;
		}

		@Override
		public DataTypeConfigurator<Object> addDataType() {
			return new DataTypeConfiguratorDecorator<Object>(dataTypeBuilder.configure(loader, schemaProxy)) {
				@Override
				public DataType<Object> create() {
					DataType<Object> dataType = super.create();
					typeSet.add(dataType);
					return dataType;
				}
			};
		}

		@Override
		public SchemaConfigurator addDataType(String name,
				Function<DataTypeConfigurator<Object>, DataTypeConfigurator<?>> configuration) {
			if (qualifiedName == null) {
				pendingDataTypeConfigurations.put(name, configuration);
			} else {
				addDataType(new QualifiedName(name, qualifiedName.getNamespace()), configuration);
			}
			return null;
		}

		@Override
		public ModelConfigurator<Object> addModel() {
			return new ModelConfiguratorDecorator<Object>(modelBuilder.configure(loader, schemaProxy)) {
				@Override
				public Model<Object> create() {
					Model<Object> model = super.create();
					modelSet.add(model);
					return model;
				}
			};
		}

		@Override
		public SchemaConfigurator addModel(String name,
				Function<ModelConfigurator<Object>, ModelConfigurator<?>> configuration) {
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

		@Override
		public <T> DataType<T> generateDataType(TypeToken<T> type) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public SchemaConfigurator configure(DataLoader loader) {
		return new SchemaConfiguratorImpl(loader);
	}
}
