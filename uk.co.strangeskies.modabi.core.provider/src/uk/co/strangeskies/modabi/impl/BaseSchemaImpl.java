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

import static uk.co.strangeskies.mathematics.Range.between;
import static uk.co.strangeskies.modabi.schema.bindingconditions.OccurrencesCondition.occurrences;
import static uk.co.strangeskies.reflection.AnnotatedWildcardTypes.wildcard;
import static uk.co.strangeskies.reflection.Types.wrapPrimitive;
import static uk.co.strangeskies.reflection.codegen.InvocationExpression.invokeResolvedStatic;
import static uk.co.strangeskies.reflection.codegen.InvocationExpression.invokeStatic;
import static uk.co.strangeskies.reflection.token.TypeToken.forAnnotatedType;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.Models;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.SchemaConfigurator;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.DataItem;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.io.Primitive;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.provisions.DereferenceSource;
import uk.co.strangeskies.modabi.processing.provisions.ImportSource;
import uk.co.strangeskies.modabi.processing.provisions.ImportTarget;
import uk.co.strangeskies.modabi.processing.provisions.IncludeTarget;
import uk.co.strangeskies.modabi.processing.provisions.ReferenceTarget;
import uk.co.strangeskies.modabi.schema.DataLoader;
import uk.co.strangeskies.modabi.schema.IOConfigurator;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;
import uk.co.strangeskies.modabi.schema.ModelFactory;
import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.Annotations;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.Types;
import uk.co.strangeskies.reflection.token.TypeArgument;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypeToken.Infer;
import uk.co.strangeskies.utilities.Enumeration;

public class BaseSchemaImpl implements BaseSchema {
	private interface ModelHelper {
		<T> Model<T> apply(String name, Function<ModelConfigurator, ModelFactory<T>> type);
	}

	private class DerivedImpl implements Derived {
		private final Model<Object> referenceModel;
		private final Model<Object> bindingReferenceModel;
		private final Model<DataSource> bufferedDataModel;
		private final Model<DataItem<?>> bufferedDataItemModel;

		private final Model<Package> packageModel;
		private final Model<Class<?>> classModel;
		private final Model<Type> typeModel;
		private final Model<AnnotatedType> annotatedTypeModel;
		private final Model<TypeToken<?>> typeTokenModel;
		private final Model<Enum<?>> enumModel;
		private final Model<Enumeration<?>> enumerationModel;
		private final Model<Range<Integer>> rangeModel;
		private final Model<Object[]> arrayModel;
		private final Model<Collection<?>> collectionModel;
		private final Model<List<?>> listModel;
		private final Model<Set<?>> setModel;
		private final Model<Object> importModel;
		private final Model<Collection<?>> includeModel;
		private final Model<URI> uriModel;
		private final Model<URL> urlModel;

		private final Model<Map<?, ?>> mapModel;

		public DerivedImpl(ModelHelper factory, Model<Enumeration<?>> enumerationBaseType) {
			Namespace namespace = BaseSchema.QUALIFIED_NAME.getNamespace();

			arrayModel = factory.apply(
					"array",
					t -> t
							.withNode(new @Infer TypeToken<Object[]>() {})
							.initializeInput(i -> i.provide(new TypeToken<List<?>>() {}))
							.initializeOutput(o -> invokeResolvedStatic(Arrays.class, "asList", o.parent()))
							.addChildBindingPoint(
									c -> c
											.name("element")
											.input(i -> i.target().invokeResolvedMethod("add", i.result()))
											.output(o -> o.iterate(o.source()))
											.bindingCondition(occurrences(between(0, null)))
											.withoutNode(forAnnotatedType(wildcard(Annotations.from(Infer.class)))))
							.addChildBindingPoint(
									c -> c
											.name("toArray")
											.input(i -> i.target().assign(i.target().invokeResolvedMethod("toArray")))
											.output(IOConfigurator::none)
											.withoutNode(void.class))
							.endNode());

			collectionModel = factory.apply(
					"collection",
					t -> t
							.withNode(new @Infer TypeToken<Collection<?>>() {})
							.initializeInput(i -> i.provide())
							.addChildBindingPoint(
									c -> c
											.name("element")
											.input(i -> i.target().invokeResolvedMethod("add", i.result()))
											.output(o -> o.iterate(o.source()))
											.bindingCondition(occurrences(between(0, null)))
											.withoutNode(forAnnotatedType(wildcard(Annotations.from(Infer.class)))))
							.endNode());

			listModel = factory
					.apply("list", t -> t.withNode(new @Infer TypeToken<List<?>>() {}).baseModel(collectionModel).endNode());

			setModel = factory
					.apply("set", t -> t.withNode(new @Infer TypeToken<Set<?>>() {}).baseModel(collectionModel).endNode());

			uriModel = factory.apply(
					"uri",
					t -> t
							.withNode(URI.class)
							.addChildBindingPoint(
									u -> u
											.name("uriString")
											.input(
													i -> i.target().assign(
															invokeStatic(
																	forClass(URI.class).constructors().resolveOverload(String.class),
																	i.result())))
											.output(o -> o.source().invokeResolvedMethod("toString"))
											.withNode(primitive(Primitive.STRING))
											.endNode())
							.endNode());

			urlModel = factory.apply(
					"url",
					t -> t
							.withNode(URL.class)
							.addChildBindingPoint(
									u -> u
											.name("urlString")
											.input(
													i -> i.target().assign(
															invokeStatic(
																	forClass(URL.class).constructors().resolveOverload(String.class),
																	i.result())))
											.output(o -> o.source().invokeResolvedMethod("toString"))
											.withNode(primitive(Primitive.STRING))
											.endNode())
							.endNode());

			bufferedDataModel = factory.apply(
					"bufferedData",
					t -> t
							.withNode(DataSource.class)
							.initializeInput(i -> i.provide(DataSource.class))
							.initializeOutput(o -> o.parent().invokeResolvedMethod("pipe", o.provide(DataTarget.class)))
							.endNode());

			bufferedDataItemModel = factory.apply(
					"bufferedDataItem",
					t -> t
							.withNode(new TypeToken<DataItem<?>>() {})
							.initializeInput(i -> i.provide(DataSource.class).invokeResolvedMethod("get"))
							.initializeOutput(o -> o.provide(DataTarget.class).invokeResolvedMethod("put", o.parent()))
							.endNode());

			@SuppressWarnings("unchecked")
			Model<Object> referenceBaseModel = (Model<Object>) factory.apply(
					"referenceBase",
					t -> t
							.export(false)
							.withNode(forAnnotatedType(wildcard(Annotations.from(Infer.class))))
							.concrete(false)
							.addChildBindingPoint(
									d -> d
											.name("targetModel")
											.input(IOConfigurator::none)
											.output(IOConfigurator::none)
											.withNode(new @Infer TypeToken<Model<?>>() {})
											.valueResolution(ValueResolution.DECLARATION_TIME)
											.endNode())
							.addChildBindingPoint(
									d -> d
											.name("targetId")
											.input(IOConfigurator::none)
											.output(IOConfigurator::none)
											.withNode(listModel)
											.concrete(false)
											.valueResolution(ValueResolution.DECLARATION_TIME)
											.addChildBindingPoint(
													e -> e.name("element").withNode(primitives.get(Primitive.QUALIFIED_NAME)).endNode())
											.endNode())
							.addChildBindingPoint(
									d -> d
											.name("data")
											.input(
													i -> i.target().assign(
															i.provide(DereferenceSource.class).invokeResolvedMethod(
																	"dereference",
																	i.bound("targetModel"),
																	i.bound("targetId"),
																	i.result())))
											.output(
													o -> o.provide(ReferenceTarget.class).invokeResolvedMethod(
															"reference",
															o.bound("targetModel"),
															o.bound("targetId"),
															o.source()))
											.withNode(bufferedDataModel)
											.endNode())
							.endNode());

			referenceModel = factory.apply(
					"reference",
					t -> t
							.withNode(referenceBaseModel)
							.concrete(false)
							.addChildBindingPoint(
									c -> c
											.name("targetModel")
											.withNode(new @Infer TypeToken<Model<?>>() {})
											.baseModel(referenceBaseModel)
											.concrete(false)
											.addChildBindingPoint(
													d -> d
															.name("targetModel")
															.withNode(new @Infer TypeToken<Model<?>>() {})
															.baseModel(referenceBaseModel)
															.provideValue(
																	new BufferingDataTarget()
																			.put(Primitive.QUALIFIED_NAME, new QualifiedName("model", namespace))
																			.buffer())
															.addChildBindingPoint(
																	e -> e
																			.name("targetModel")
																			.withNode(new @Infer TypeToken<Model<?>>() {})
																			.baseModel(referenceBaseModel)
																			.concrete(false)
																			.provideValue(
																					new BufferingDataTarget()
																							.put(Primitive.QUALIFIED_NAME, new QualifiedName("model", namespace))
																							.buffer())
																			.endNode())
															.addChildBindingPoint(
																	e -> e
																			.name("targetId")
																			.withNode()
																			.provideValue(
																					new BufferingDataTarget()
																							.put(
																									Primitive.QUALIFIED_NAME,
																									new QualifiedName("configurator", namespace))
																							.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace))
																							.buffer())
																			.endNode())
															.endNode())
											.addChildBindingPoint(
													d -> d
															.name("targetId")
															.withNode()
															.provideValue(
																	new BufferingDataTarget()
																			.put(Primitive.QUALIFIED_NAME, new QualifiedName("configurator", namespace))
																			.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace))
																			.buffer())
															.endNode())
											.endNode())
							.endNode());

			@SuppressWarnings("unchecked")
			Model<Object> bindingReferenceModel = (Model<Object>) factory.apply(
					"bindingReference",
					t -> t
							.withNode(forAnnotatedType(wildcard(Annotations.from(Infer.class))))
							.concrete(false)
							.initializeInput(i -> i.provide(DereferenceSource.class))
							.addChildBindingPoint(
									c -> c
											.name("targetNode")
											.input(IOConfigurator::none)
											.output(IOConfigurator::none)
											.withNode(referenceModel)
											.addChildBindingPoint(
													d -> d
															.name("targetModel")
															.withNode()
															.provideValue(
																	new BufferingDataTarget()
																			.put(Primitive.QUALIFIED_NAME, new QualifiedName("binding", namespace))
																			.buffer())
															.endNode())
											.addChildBindingPoint(
													e -> e
															.name("targetId")
															.withNode()
															.provideValue(
																	new BufferingDataTarget()
																			.put(Primitive.QUALIFIED_NAME, new QualifiedName("configurator", namespace))
																			.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace))
																			.buffer())
															.endNode())
											.endNode())
							.endNode());
			this.bindingReferenceModel = bindingReferenceModel;

			packageModel = factory.apply(
					"package",
					t -> t
							.withNode(Package.class)
							.addChildBindingPoint(
									p -> p
											.name("name")
											.input(i -> invokeResolvedStatic(Package.class, "getPackage"))
											.output(o -> o.source().invokeResolvedMethod("getName"))
											.withNode(primitives.get(Primitive.STRING))
											.endNode())
							.endNode());

			typeModel = factory.apply(
					"type",
					t -> t
							.withNode(Type.class)
							.addChildBindingPoint(
									p -> p
											.name("name")
											.input(i -> invokeResolvedStatic(Types.class, "fromString", i.result()))
											.output(o -> invokeResolvedStatic(Types.class, "toString", o.source()))
											.withNode(primitives.get(Primitive.STRING))
											.endNode())
							.endNode());

			classModel = factory.apply("class", t -> t.withNode(new TypeToken<Class<?>>() {}).baseModel(typeModel).endNode());

			annotatedTypeModel = factory.apply(
					"annotatedType",
					t -> t
							.withNode(AnnotatedType.class)
							.addChildBindingPoint(
									p -> p
											.name("name")
											.input(i -> invokeResolvedStatic(AnnotatedTypes.class, "fromString", i.result()))
											.output(o -> invokeResolvedStatic(AnnotatedTypes.class, "toString", o.source()))
											.withNode(primitives.get(Primitive.STRING))
											.endNode())
							.endNode());

			typeTokenModel = factory.apply(
					"typeToken",
					t -> t
							.withNode(new TypeToken<TypeToken<?>>() {})
							.addChildBindingPoint(
									c -> c
											.input(i -> invokeResolvedStatic(TypeToken.class, "overAnnotatedType", i.result()))
											.output(o -> o.source().invokeResolvedMethod("getAnnotatedDeclaration"))
											.withNode(annotatedTypeModel)
											.endNode())
							.endNode());

			enumModel = factory.apply(
					"enum",
					t -> t
							.withNode(new TypeToken<Enum<?>>() {})
							.concrete(false)
							.addChildBindingPoint(
									c -> c
											.name("enumType")
											.input(IOConfigurator::none)
											.output(IOConfigurator::none)
											.withNode(new TypeToken<Class<? extends Enum<?>>>() {})
											.provideValue(new BufferingDataTarget().buffer())
											.initializeInput(
													i -> i
															.provide(ProcessingContext.class)
															.invokeResolvedMethod("getBindingNode")
															.invokeResolvedMethod("dataType")
															.invokeResolvedMethod("getRawType"))
											.endNode())
							.addChildBindingPoint(
									p -> p
											.name("name")
											.input(
													i -> invokeResolvedStatic(Enumeration.class, "valueOfEnum", i.bound("enumType"), i.result()))
											.output(o -> o.source().invokeResolvedMethod("getName"))
											.withNode(primitives.get(Primitive.STRING))
											.endNode())
							.endNode());

			enumerationModel = factory.apply(
					"enumeration",
					t -> t
							.withNode(enumerationBaseType)
							.concrete(false)
							.addChildBindingPoint(
									c -> c
											.name("enumType")
											.input(IOConfigurator::none)
											.output(IOConfigurator::none)
											.withNode(new TypeToken<Class<? extends Enumeration<?>>>() {})
											.provideValue(new BufferingDataTarget().buffer())
											.initializeInput(
													i -> i
															.provide(ProcessingContext.class)
															.invokeResolvedMethod("getBindingNode")
															.invokeResolvedMethod("dataType")
															.invokeResolvedMethod("getRawType"))
											.endNode())
							.addChildBindingPoint(
									p -> p
											.name("name")
											.input(i -> invokeResolvedStatic(Enumeration.class, "valueOf", i.bound("enumType"), i.result()))
											.output(o -> o.source().invokeResolvedMethod("getName"))
											.withNode(primitives.get(Primitive.STRING))
											.endNode())
							.endNode());

			rangeModel = factory.apply(
					"range",
					t -> t
							.withNode(new TypeToken<Range<Integer>>() {})
							.addChildBindingPoint(
									p -> p
											.name("string")
											.input(i -> invokeResolvedStatic(Range.class, "parse", i.result()))
											.output(o -> invokeResolvedStatic(Range.class, "compose", o.source()))
											.withNode(primitives.get(Primitive.STRING))
											.endNode())
							.endNode());

			includeModel = factory.apply(
					"include",
					t -> t
							.withNode(new @Infer TypeToken<Collection<?>>() {})
							.concrete(false)
							.initializeInput(i -> i.parent())
							.initializeOutput(o -> o.parent())
							.addChildBindingPoint(
									c -> c
											.name("targetModel")
											.input(IOConfigurator::none)
											.output(IOConfigurator::none)
											.withNode(new TypeToken<Model<?>>() {})
											.concrete(false)
											.baseModel((Model<Object>) referenceModel)
											.valueResolution(ValueResolution.DECLARATION_TIME)
											.addChildBindingPoint(
													d -> d
															.name("targetModel")
															.withNode()
															.provideValue(
																	new BufferingDataTarget()
																			.put(Primitive.QUALIFIED_NAME, new QualifiedName("model", namespace))
																			.buffer())
															.endNode())
											.addChildBindingPoint(
													d -> d
															.name("targetId")
															.withNode()
															.provideValue(
																	new BufferingDataTarget()
																			.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace))
																			.buffer())
															.endNode())
											.endNode())
							.addChildBindingPoint(
									e -> e
											.name("object")
											.input(i -> invokeResolvedStatic(IncludeTarget.class, "include", i.target()))
											.output(o -> invokeResolvedStatic(IncludeTarget.class, "include", o.bound("targetModel")))
											.withNode(Collection.class)
											.endNode())
							.endNode());

			importModel = factory.apply(
					"import",
					t -> t
							.withNode(Object.class)
							.concrete(false)
							.initializeInput(i -> i.parent())
							.initializeOutput(o -> o.parent())
							.addChildBindingPoint(
									c -> c
											.name("targetModel")
											.input(IOConfigurator::none)
											.output(IOConfigurator::none)
											.withNode(new TypeToken<Model<?>>() {})
											.baseModel((Model<Object>) referenceModel)
											.concrete(false)
											.valueResolution(ValueResolution.DECLARATION_TIME)
											.addChildBindingPoint(
													d -> d
															.name("targetModel")
															.withNode()
															.provideValue(
																	new BufferingDataTarget()
																			.put(Primitive.QUALIFIED_NAME, new QualifiedName("model", namespace))
																			.buffer())
															.endNode())
											.addChildBindingPoint(
													d -> d
															.name("targetId")
															.withNode()
															.provideValue(
																	new BufferingDataTarget()
																			.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace))
																			.buffer())
															.endNode())
											.endNode())
							.addChildBindingPoint(
									d -> d
											.name("targetId")
											.input(IOConfigurator::none)
											.output(IOConfigurator::none)
											.withNode(listModel)
											.concrete(false)
											.valueResolution(ValueResolution.DECLARATION_TIME)
											.addChildBindingPoint(
													e -> e.name("element").withNode(primitives.get(Primitive.QUALIFIED_NAME)).endNode())
											.endNode())
							.addChildBindingPoint(
									d -> d
											.name("data")
											.input(
													i -> i.provide(ImportSource.class).invokeResolvedMethod(
															"dereferenceImport",
															i.bound("targetModel"),
															i.bound("targetId"),
															i.result()))
											.output(
													o -> o.provide(ImportTarget.class).invokeResolvedMethod(
															"referenceImport",
															o.bound("targetModel"),
															o.bound("targetId"),
															o.source()))
											.withNode(bufferedDataModel)
											.endNode())
							.endNode());

			/*
			 * Having trouble annotating Map.Entry for some reason, so need this
			 * kludge.
			 */
			mapModel = factory.apply(
					"map",
					c -> c
							.withNode(new @Infer TypeToken<Map<?, ?>>() {})
							.addChildBindingPoint(
									f -> f
											.name("entry")
											.bindingCondition(occurrences(between(0, null)))
											.input(IOConfigurator::none)
											.output(o -> o.iterate(o.source().invokeResolvedMethod("entrySet")))
											.withNode(void.class)
											.initializeInput(i -> i.parent())
											.addChildBindingPoint(
													k -> k
															.name("key")
															.input(IOConfigurator::none)
															.output(o -> o.source().invokeResolvedMethod("getKey"))
															.withNode(forAnnotatedType(wildcard(Annotations.from(Infer.class))))
															.extensible(true)
															.endNode())
											.addChildBindingPoint(
													v -> v
															.name("value")
															.output(o -> o.source().invokeResolvedMethod("getValue"))
															.input(i -> i.target().invokeResolvedMethod("put", i.bound("key"), i.result()))
															.withNode(forAnnotatedType(wildcard(Annotations.from(Infer.class))))
															.extensible(true)
															.endNode())
											.endNode())
							.endNode());
		}

		@Override
		public Model<Package> packageModel() {
			return packageModel;
		}

		@Override
		public Model<Class<?>> classModel() {
			return classModel;
		}

		@Override
		public Model<Type> typeModel() {
			return typeModel;
		}

		@Override
		public Model<AnnotatedType> annotatedTypeModel() {
			return annotatedTypeModel;
		}

		@Override
		public Model<TypeToken<?>> typeTokenModel() {
			return typeTokenModel;
		}

		@Override
		public Model<Enum<?>> enumModel() {
			return enumModel;
		}

		@Override
		public Model<Enumeration<?>> enumerationModel() {
			return enumerationModel;
		}

		@Override
		public Model<Range<Integer>> rangeModel() {
			return rangeModel;
		}

		@Override
		public Model<?> referenceModel() {
			return referenceModel;
		}

		@Override
		public Model<?> bindingReferenceModel() {
			return bindingReferenceModel;
		}

		@Override
		public Model<DataSource> bufferedDataModel() {
			return bufferedDataModel;
		}

		@Override
		public Model<DataItem<?>> bufferedDataItemModel() {
			return bufferedDataItemModel;
		}

		@Override
		public Model<Object[]> arrayModel() {
			return arrayModel;
		}

		@Override
		public Model<Collection<?>> collectionModel() {
			return collectionModel;
		}

		@Override
		public Model<List<?>> listModel() {
			return listModel;
		}

		@Override
		public Model<Set<?>> setModel() {
			return setModel;
		}

		@Override
		public Model<Collection<?>> includeModel() {
			return includeModel;
		}

		@Override
		public Model<Object> importModel() {
			return importModel;
		}

		@Override
		public Model<URI> uriModel() {
			return uriModel;
		}

		@Override
		public Model<URL> urlModel() {
			return urlModel;
		}

		@Override
		public Model<Map<?, ?>> mapModel() {
			return mapModel;
		}
	}

	public static <K, V> Map.Entry<K, V> mapEntry(K key, V value) {
		return null;
	}

	private final Schema baseSchema;

	private final Map<Primitive<?>, Model<?>> primitives;
	private final Derived derivedTypes;

	public BaseSchemaImpl(SchemaBuilder schemaBuilder, DataLoader loader) {
		QualifiedName name = BaseSchema.QUALIFIED_NAME;
		Namespace namespace = name.getNamespace();

		/*
		 * Schema
		 */
		SchemaConfigurator schemaConfigurator = schemaBuilder.configure(loader).qualifiedName(name);

		/*
		 * Models
		 */
		ModelHelper modelFactory = new ModelHelper() {
			@Override
			public <T> Model<T> apply(String name, Function<ModelConfigurator, ModelFactory<T>> type) {
				return type.apply(schemaConfigurator.addModel().name(name, namespace)).createModel();
			}
		};

		Model<Enumeration<?>> enumerationBaseType = modelFactory.apply(
				"enumerationBase",
				c -> c.export(false).withNode(new @Infer TypeToken<Enumeration<?>>() {}).concrete(false).endNode());

		Model<Object> primitive = modelFactory.apply(
				"primitive",
				p -> p
						.export(false)
						.withNode()
						.concrete(false)
						.addChildBindingPoint(
								c -> c
										.name("dataType")
										.input(IOConfigurator::none)
										.output(IOConfigurator::none)
										.withNode(new @Infer TypeToken<Primitive<?>>() {})
										.baseModel(enumerationBaseType)
										.concrete(false)
										.valueResolution(ValueResolution.DECLARATION_TIME)
										.endNode())
						.addChildBindingPoint(
								c -> c
										.name("io")
										.input(
												i -> i.target().assign(
														i.provide(DataSource.class).invokeResolvedMethod("get", i.bound("dataType"))))
										.output(
												o -> o.provide(DataTarget.class).invokeResolvedMethod("put", o.bound("dataType"), o.source()))
										.withNode(void.class)
										.endNode())
						.endNode());

		primitives = new HashMap<>();
		for (Primitive<?> dataType : Enumeration.getConstants(Primitive.class)) {
			primitives.put(
					dataType,
					modelFactory.apply(
							dataType.name(),
							p -> p
									.withNode(dataType.dataClass())
									.baseModel(primitive)
									.addChildBindingPoint(
											c -> c
													.name("dataType")
													.withNode(resolvePrimitiveDataType(dataType))
													.provideValue(new BufferingDataTarget().put(Primitive.STRING, dataType.name()).buffer())
													.endNode())
									.endNode()));
		}

		derivedTypes = new DerivedImpl(modelFactory, enumerationBaseType);

		/*
		 * Schema
		 */
		baseSchema = schemaConfigurator.create();
	}

	private <T> TypeToken<Primitive<T>> resolvePrimitiveDataType(Primitive<T> dataType) {
		return new TypeToken<Primitive<T>>() {}
				.withTypeArguments(new TypeArgument<T>(wrapPrimitive(dataType.dataClass())) {});
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Model<T> primitive(Primitive<T> type) {
		return (Model<T>) primitives.get(type);
	}

	@Override
	public Derived derived() {
		return derivedTypes;
	}

	/* Schema */

	@Override
	public QualifiedName qualifiedName() {
		return baseSchema.qualifiedName();
	}

	@Override
	public Schemata dependencies() {
		return baseSchema.dependencies();
	}

	@Override
	public Models models() {
		return baseSchema.models();
	}

	@Override
	public boolean equals(Object obj) {
		return baseSchema.equals(obj);
	}

	@Override
	public int hashCode() {
		return baseSchema.hashCode();
	}

	@Override
	public Imports imports() {
		return Imports.empty();
	}

	@Override
	public String toString() {
		return qualifiedName().toString();
	}
}
