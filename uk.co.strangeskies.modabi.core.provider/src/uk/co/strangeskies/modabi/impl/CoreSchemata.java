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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.DataType;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataNode.Format;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.building.DataBindingTypeBuilder;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.modabi.schema.building.ModelBuilder;
import uk.co.strangeskies.utilities.Enumeration;

public class CoreSchemata {
	private final BaseSchema baseSchema;
	private final MetaSchema metaSchema;

	public CoreSchemata(SchemaBuilder schemaBuilder, ModelBuilder modelBuilder,
			DataBindingTypeBuilder dataTypeBuilder) {
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
						return (List<T>) Arrays
								.asList(Format.valueOf(data.get(DataType.STRING)));

					if (node.getName().getName().equals("dataType"))
						return (List<T>) Arrays.asList(
								Enumeration.valueOf(DataType.class, data.get(DataType.STRING)));

					if (node.getName().getName().equals("targetId"))
						return (List<T>) Arrays.asList(data.get(DataType.QUALIFIED_NAME));

					if (node.getName().getName().equals("inline"))
						return (List<T>) Arrays.asList(data.get(DataType.BOOLEAN));

					if (node.getName().getName().equals("targetModel")) {
						QualifiedName name = data.get(DataType.QUALIFIED_NAME);

						Supplier<Model<?>> objectProvider = () -> {
							Model<?> model = baseSchema.getModels().get(name);

							if (model == null)
								model = metaSchema.getModels().get(name);

							if (model == null)
								throw new SchemaException("Cannot provide model '" + name
										+ "' from base schema or metaschema.");

							return model;
						};

						return (List<T>) Arrays
								.asList(Proxy.newProxyInstance(Model.class.getClassLoader(),
										new Class[] { Model.class }, new InvocationHandler() {
							private Model<?> model;

							@Override
							public Object invoke(Object proxy, Method method, Object[] args)
									throws Throwable {
								if (model == null)
									model = objectProvider.get();

								return method.invoke(model, args);
							}
						}));
					}

					if (node.getName().getName().equals("enumType"))
						return (List<T>) Arrays.asList(Enum.class);

					if (node.getName().getName().equals("enumerationType"))
						return (List<T>) Arrays.asList(Enumeration.class);
				}

				throw new SchemaException(
						"Unable to provide value for node '" + node + "'");
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
