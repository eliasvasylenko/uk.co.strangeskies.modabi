/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.osgi.service.component.annotations.Component;

import uk.co.strangeskies.modabi.DataTypes;
import uk.co.strangeskies.modabi.Models;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.SchemaConfigurator;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.Model;

@Component
public class SchemaBuilderImpl implements SchemaBuilder {
	public class SchemaConfiguratorImpl implements SchemaConfigurator {
		private final Set<DataType<?>> typeSet;
		private QualifiedName qualifiedName;
		private final Set<Model<?>> modelSet;
		private final Schemata dependencySet;

		public SchemaConfiguratorImpl() {
			typeSet = new LinkedHashSet<>();
			modelSet = new LinkedHashSet<>();
			dependencySet = new Schemata();
		}

		@Override
		public Schema create() {
			final QualifiedName qualifiedName = this.qualifiedName;
			final DataTypes types = new DataTypes();
			types.addAll(typeSet);
			final Models models = new Models();
			models.addAll(modelSet);
			final Schemata dependencies = new Schemata();
			dependencies.addAll(dependencySet);

			return new Schema() {
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
				public boolean equals(Object obj) {
					if (!(obj instanceof Schema))
						return false;

					if (obj == this)
						return true;

					Schema other = (Schema) obj;

					return getQualifiedName().equals(other.getQualifiedName())
							&& getModels().equals(other.getModels())
							&& getDataTypes().equals(other.getDataTypes())
							&& getDependencies().equals(other.getDependencies());
				}

				@Override
				public int hashCode() {
					return getDataTypes().hashCode() ^ getQualifiedName().hashCode()
							^ getModels().hashCode() ^ getDependencies().hashCode();
				}
			};
		}

		@Override
		public SchemaConfigurator qualifiedName(QualifiedName name) {
			qualifiedName = name;

			return this;
		}

		@Override
		public SchemaConfigurator types(
				Collection<? extends DataType<?>> types) {
			typeSet.clear();
			typeSet.addAll(types);

			return this;
		}

		@Override
		public SchemaConfigurator models(Collection<? extends Model<?>> models) {
			modelSet.clear();
			modelSet.addAll(models);

			return this;
		}

		@Override
		public SchemaConfigurator dependencies(
				Collection<? extends Schema> dependencies) {
			dependencySet.clear();
			dependencySet.addAll(dependencies);

			return this;
		}
	}

	@Override
	public SchemaConfigurator configure() {
		return new SchemaConfiguratorImpl();
	}
}
