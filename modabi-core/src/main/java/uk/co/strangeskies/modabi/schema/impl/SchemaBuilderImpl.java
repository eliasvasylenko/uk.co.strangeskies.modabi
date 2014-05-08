package uk.co.strangeskies.modabi.schema.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.data.DataTypes;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.Models;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.SchemaBuilder;
import uk.co.strangeskies.modabi.schema.SchemaConfigurator;
import uk.co.strangeskies.modabi.schema.Schemata;

public class SchemaBuilderImpl implements SchemaBuilder {
	public class SchemaConfiguratorImpl implements SchemaConfigurator {
		private final Set<DataType<?>> typeSet;
		private QualifiedName qualifiedName;
		private final Set<Model<?>> modelSet;
		private final Schemata dependencySet;

		public SchemaConfiguratorImpl() {
			typeSet = new HashSet<>();
			modelSet = new LinkedHashSet<>();
			dependencySet = new Schemata();
		}

		@Override
		public Schema create() {
			final QualifiedName qualifiedName = this.qualifiedName;
			final DataTypes types = new DataTypes(qualifiedName.getNamespace());
			types.addAll(typeSet);
			final Models models = new Models(qualifiedName.getNamespace());
			models.addAll(modelSet);
			final Schemata dependencies = dependencySet;

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
			};
		}

		@Override
		public SchemaConfigurator qualifiedName(QualifiedName name) {
			qualifiedName = name;

			return this;
		}

		@Override
		public SchemaConfigurator types(Collection<? extends DataType<?>> types) {
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
