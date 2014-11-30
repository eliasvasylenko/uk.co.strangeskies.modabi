package uk.co.strangeskies.modabi.schema.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.proxy.ObjectProvider;
import org.apache.commons.proxy.ProxyFactory;
import org.apache.commons.proxy.provider.SingletonProvider;

import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.DataType;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.BaseSchema;
import uk.co.strangeskies.modabi.schema.MetaSchema;
import uk.co.strangeskies.modabi.schema.SchemaBuilder;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.DataNode.Format;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.node.model.ModelBuilder;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingTypeBuilder;
import uk.co.strangeskies.utilities.Enumeration;

public class CoreSchemata {
	private final BaseSchema baseSchema;
	private final MetaSchema metaSchema;

	public CoreSchemata(SchemaBuilder schemaBuilder, ModelBuilder modelBuilder,
			DataBindingTypeBuilder dataTypeBuilder) {
		List<Object> objs = new ArrayList<>();

		/*
		 * We obviously don't have have any schema to use to bind provided values
		 * which have registration time resolution, since what we're doing here is
		 * building our core schema for the first time, so we must manually provide
		 * them with a custom loader.
		 */
		DataLoader loader = new DataLoader() {
			@SuppressWarnings("unchecked")
			@Override
			public <T> List<T> loadData(DataNode<T> node, DataSource data) {
				Namespace namespace = new Namespace(BaseSchema.class.getPackage(),
						LocalDate.of(2014, 1, 1));

				if (node.getName().getNamespace().equals(namespace)) {
					if (node.getName().getName().equals("configure"))
						return Collections.emptyList();

					if (node.getName().getName().equals("format"))
						return (List<T>) Arrays.asList(Format.valueOf(data
								.get(DataType.STRING)));

					if (node.getName().getName().equals("dataType"))
						return (List<T>) Arrays.asList(Enumeration.valueOf(DataType.class,
								data.get(DataType.STRING)));

					if (node.getName().getName().equals("targetId"))
						return (List<T>) Arrays.asList(data.get(DataType.QUALIFIED_NAME));

					if (node.getName().getName().equals("inline"))
						return (List<T>) Arrays.asList(data.get(DataType.BOOLEAN));

					if (node.getName().getName().equals("targetModel")) {
						QualifiedName name = data.get(DataType.QUALIFIED_NAME);

						ObjectProvider objectProvider = () -> {
							Model<?> model = baseSchema.getModels().get(name);

							if (model == null)
								model = metaSchema.getModels().get(name);

							if (model == null)
								throw new SchemaException("Cannot provide model '" + name
										+ "' from base schema or metaschema.");

							return model;
						};

						objs.add(new ProxyFactory().createDelegatorProxy(objectProvider,
								new Class[] { Model.class }));

						return (List<T>) Arrays.asList(new ProxyFactory()
								.createDelegatorProxy(new SingletonProvider(objectProvider),
										new Class[] { Model.class }));
					}

					if (node.getName().getName().equals("enumType"))
						return (List<T>) Arrays.asList(Enum.class);

					if (node.getName().getName().equals("enumerationType"))
						return (List<T>) Arrays.asList(Enumeration.class);
				}

				throw new SchemaException("Unable to provide value for node '" + node
						+ "'.");
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
