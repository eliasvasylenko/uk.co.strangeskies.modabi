package uk.co.strangeskies.modabi.schema.processing.impl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.modabi.data.DataBindingTypeBuilder;
import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.data.io.DataType;
import uk.co.strangeskies.modabi.model.building.DataLoader;
import uk.co.strangeskies.modabi.model.building.ModelBuilder;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode.Format;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.schema.BaseSchema;
import uk.co.strangeskies.modabi.schema.MetaSchema;
import uk.co.strangeskies.modabi.schema.SchemaBuilder;

public class CoreSchemata {
	private final BaseSchema baseSchema;
	private final MetaSchema metaSchema;

	public CoreSchemata(SchemaBuilder schemaBuilder, ModelBuilder modelBuilder,
			DataBindingTypeBuilder dataTypeBuilder) {
		DataLoader loader = new DataLoader() {
			@SuppressWarnings("unchecked")
			@Override
			public <T> List<T> loadData(DataNode<T> node, BufferedDataSource data) {
				System.out.println(node.getName() + ": " + data);

				Namespace namespace = new Namespace(BaseSchema.class.getPackage(),
						LocalDate.of(2014, 1, 1));

				if (node.getName().getNamespace().equals(namespace)
						&& node.getName().getName().equals("format"))
					return (List<T>) Arrays.asList(Format.valueOf(data
							.get(DataType.STRING)));

				return null;
			}
		};
		baseSchema = new BaseSchemaImpl(schemaBuilder, modelBuilder,
				dataTypeBuilder, loader);
		metaSchema = new MetaSchemaImpl(schemaBuilder, modelBuilder,
				dataTypeBuilder, loader, baseSchema);
	}

	public BaseSchema baseSchema() {
		return baseSchema;
	}

	public MetaSchema metaSchema() {
		return metaSchema;
	}
}
