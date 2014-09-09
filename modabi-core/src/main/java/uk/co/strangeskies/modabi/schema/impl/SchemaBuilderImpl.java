package uk.co.strangeskies.modabi.schema.impl;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.DataBindingTypes;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.SchemaBuilder;
import uk.co.strangeskies.modabi.schema.SchemaConfigurator;
import uk.co.strangeskies.modabi.schema.Schemata;
import uk.co.strangeskies.modabi.schema.model.Model;
import uk.co.strangeskies.modabi.schema.model.Models;
import uk.co.strangeskies.modabi.schema.requirement.Requirement;
import uk.co.strangeskies.modabi.schema.requirement.Requirements;

public class SchemaBuilderImpl implements SchemaBuilder {
	public class SchemaConfiguratorImpl implements SchemaConfigurator {
		private final Set<DataBindingType<?>> typeSet;
		private QualifiedName qualifiedName;
		private final Set<Model<?>> modelSet;
		private final Schemata dependencySet;
		private Set<Requirement<?>> requirementSet;

		public SchemaConfiguratorImpl() {
			typeSet = new LinkedHashSet<>();
			modelSet = new LinkedHashSet<>();
			dependencySet = new Schemata();
			requirementSet = new LinkedHashSet<>();
		}

		@Override
		public Schema create() {
			final QualifiedName qualifiedName = this.qualifiedName;
			final DataBindingTypes types = new DataBindingTypes();
			types.addAll(typeSet);
			final Models models = new Models();
			models.addAll(modelSet);
			final Schemata dependencies = new Schemata();
			dependencies.addAll(dependencySet);
			final Requirements requirements = () -> requirementSet;

			return new Schema() {
				@Override
				public DataBindingTypes getDataTypes() {
					return types;
				}

				@Override
				public Requirements getRequirements() {
					return requirements;
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
		public SchemaConfigurator requirements(
				Set<? extends Requirement<?>> requirements) {
			requirementSet.clear();
			requirementSet.addAll(requirements);

			return this;
		}

		@Override
		public SchemaConfigurator types(
				Collection<? extends DataBindingType<?>> types) {
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
