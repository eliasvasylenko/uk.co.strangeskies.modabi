package uk.co.strangeskies.modabi.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.SchemaConfigurator;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.data.DataTypes;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.Models;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public class SchemaBuilderImpl implements SchemaBuilder {
	@Override
	public SchemaConfigurator configure() {
		return new SchemaConfigurator() {
			private Set<DataType<?>> typeSet = new HashSet<>();
			private QualifiedName qualifiedName;
			private Set<Model<?>> modelSet = new HashSet<>();
			private Schemata dependencySet;

			@Override
			public Schema create() {
				final DataTypes types = new DataTypes(qualifiedName.getNamespace());
				types.addAll(typeSet);
				final Models models = new Models(qualifiedName.getNamespace());
				models.addAll(modelSet);

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
						return dependencySet;
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
				dependencySet = new Schemata();
				dependencySet.addAll(dependencies);

				return this;
			}
		};
	}
}
