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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.Primitive;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataNode.Format;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.Enumeration;

/**
 * Class for bootstrapping core schemata, i.e. an implementation of
 * {@link BaseSchema} and {@link MetaSchema}.
 * 
 * @author Elias N Vasylenko
 */
public class CoreSchemata {
	private class TargetModelSkeletonObject {
		private final QualifiedName name;
		private final TypeToken<?> type;

		public TargetModelSkeletonObject(QualifiedName name) {
			this.name = name;

			switch (name.getName()) {
			case "model":
				type = modelOf(new TypeToken<Model<?>>() {});
				break;
			case "binding":
				type = modelOf(new TypeToken<BindingNode<?, ?>>() {});
				break;
			case "type":
				type = modelOf(new TypeToken<DataType<?>>() {});
				break;
			case "schema":
				type = new TypeToken<Model<Schema>>() {};
				break;
			default:
				type = null;
			}
		}

		private <T> TypeToken<Model<T>> modelOf(TypeToken<T> type) {
			return new TypeToken<Model<T>>() {}.withTypeArgument(new TypeParameter<T>() {}, type);
		}

		boolean isReady() {
			return baseSchema != null && metaSchema != null;
		}

		Model<?> provideObject() {
			Model<?> model = baseSchema.models().get(name);

			if (model == null)
				model = metaSchema.models().get(name);

			if (model == null)
				throw new ModabiException(t -> t.noBootstrapModelFound(name));

			return model;
		}

		public boolean hasThisType() {
			return type != null;
		}

		public TypeToken<?> getThisType() {
			return type;
		}
	}

	private final BaseSchema baseSchema;
	private final MetaSchema metaSchema;

	public CoreSchemata(SchemaBuilder schemaBuilder) {
		Map<QualifiedName, Model<?>> targetModels = new HashMap<>();

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
				Namespace namespace = new Namespace(BaseSchema.class.getPackage(), LocalDate.of(2014, 1, 1));

				if (node.name().getNamespace().equals(namespace)) {
					if (node.name().getName().equals("configure"))
						return Collections.emptyList();

					if (node.name().getName().equals("format"))
						return (List<T>) Arrays.asList(Format.valueOf(data.get(Primitive.STRING)));

					if (node.name().getName().equals("dataType"))
						return (List<T>) Arrays.asList(Enumeration.valueOf(Primitive.class, data.get(Primitive.STRING)));

					if (node.name().getName().equals("targetId"))
						return (List<T>) Arrays.asList(data.get(Primitive.QUALIFIED_NAME));

					if (node.name().getName().equals("inline"))
						return (List<T>) Arrays.asList(data.get(Primitive.BOOLEAN));

					if (node.name().getName().equals("isExternal"))
						return (List<T>) Arrays.asList(data.get(Primitive.BOOLEAN));

					if (node.name().getName().equals("enumType"))
						return (List<T>) Arrays.asList(Enum.class);

					if (node.name().getName().equals("enumerationType"))
						return (List<T>) Arrays.asList(Enumeration.class);

					if (node.name().getName().equals("targetModel")) {
						QualifiedName name = data.get(Primitive.QUALIFIED_NAME);

						return (List<T>) Arrays.asList(targetModels.computeIfAbsent(name, n -> {

							return (Model<?>) Proxy.newProxyInstance(Model.class.getClassLoader(), new Class[] { Model.class },
									new InvocationHandler() {
										private TargetModelSkeletonObject skeleton = new TargetModelSkeletonObject(name);
										private Model<?> model;

										@Override
										public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
											if (skeleton != null) {
												if (skeleton.isReady()) {
													model = skeleton.provideObject();
													skeleton = null;
												} else {
													if (method.getName().equals("getThisType") && skeleton.hasThisType()) {
														return skeleton.getThisType();
													}
													throw new IllegalStateException("Proxy for target model '" + name + "' is not ready yet");
												}
											}

											return method.invoke(model, args);
										}
									});
						}));
					}
				}

				throw new ModabiException(t -> t.noBootstrapValueFound(node.name()));
			}
		};
		baseSchema = new BaseSchemaImpl(schemaBuilder, loader);
		metaSchema = new MetaSchemaImpl(schemaBuilder, loader, baseSchema);

		targetModels.values().forEach(Model::getThis);
	}

	public BaseSchema baseSchema() {
		return baseSchema;
	}

	public MetaSchema metaSchema() {
		return metaSchema;
	}
}
