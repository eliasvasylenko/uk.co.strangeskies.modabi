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
import static uk.co.strangeskies.modabi.schema.BindingCondition.occurrences;
import static uk.co.strangeskies.reflection.AnnotatedWildcardTypes.unbounded;
import static uk.co.strangeskies.reflection.Annotations.from;
import static uk.co.strangeskies.reflection.TypeToken.overAnnotatedType;
import static uk.co.strangeskies.reflection.TypeToken.overType;
import static uk.co.strangeskies.reflection.codegen.InvocationExpression.invokeResolvedStatic;
import static uk.co.strangeskies.reflection.codegen.InvocationExpression.invokeStatic;

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
import uk.co.strangeskies.modabi.processing.providers.DereferenceSource;
import uk.co.strangeskies.modabi.processing.providers.ImportSource;
import uk.co.strangeskies.modabi.processing.providers.ImportTarget;
import uk.co.strangeskies.modabi.processing.providers.IncludeTarget;
import uk.co.strangeskies.modabi.processing.providers.ReferenceTarget;
import uk.co.strangeskies.modabi.schema.DataLoader;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;
import uk.co.strangeskies.reflection.AnnotatedParameterizedTypes;
import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.AnnotatedWildcardTypes;
import uk.co.strangeskies.reflection.Annotations;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypeToken.Infer;
import uk.co.strangeskies.reflection.Types;
import uk.co.strangeskies.utilities.Enumeration;

public class BaseSchemaImpl implements BaseSchema {
	private interface ModelFactory {
		<T> Model<T> apply(String name, Function<ModelConfigurator<?>, ModelConfigurator<T>> type);
	}

	private class DerivedImpl implements Derived {
		private final Model<Object> referenceType;
		private final Model<Object> bindingReferenceType;
		private final Model<DataSource> bufferedDataType;
		private final Model<DataItem<?>> bufferedDataItemType;

		private final Model<Package> packageType;
		private final Model<Class<?>> classType;
		private final Model<Type> typeType;
		private final Model<AnnotatedType> annotatedTypeType;
		private final Model<TypeToken<?>> typeTokenType;
		private final Model<Enum<?>> enumType;
		private final Model<Enumeration<?>> enumerationType;
		private final Model<Range<Integer>> rangeType;
		private final Model<Object[]> arrayType;
		private final Model<Collection<?>> collectionType;
		private final Model<List<?>> listType;
		private final Model<Set<?>> setType;
		private final Model<Object> importType;
		private final Model<Collection<?>> includeType;
		private final Model<URI> uriType;
		private final Model<URL> urlType;

		private Model<?> simpleModel;
		private Model<Map<?, ?>> mapModel;

		public DerivedImpl(ModelFactory factory, Model<Enumeration<?>> enumerationBaseType) {
			Namespace namespace = BaseSchema.QUALIFIED_NAME.getNamespace();

			arrayType = factory.apply("array",
					t -> t.dataType(new @Infer TypeToken<Object[]>() {}).node(n -> n
							.initializeInput(i -> i.provide(new TypeToken<List<?>>() {}))
							.initializeOutput(o -> invokeResolvedStatic(Arrays.class, "asList", o.parent()))
							.addChildBindingPoint(c -> c
									.name("element")
									.input(i -> i.target().invokeResolvedMethod("add", i.result()))
									.output(o -> o.iterate(o.source()))
									.concrete(false)
									.dataType(AnnotatedWildcardTypes.unbounded(Annotations.from(Infer.class)))
									.condition(occurrences(between(0, null))))
							.addChildBindingPoint(c -> c
									.name("toArray")
									.dataType(void.class)
									.input(i -> i.target().assign(i.target().invokeResolvedMethod("toArray")))
									.noOutput())));

			collectionType = factory.apply("collection", t -> t.dataType(new @Infer TypeToken<Collection<?>>() {}).node(
					n -> n.initializeInput(i -> i.provideFor(i.parent())).addChildBindingPoint(c -> c
							.name("element")
							.input(i -> i.target().invokeResolvedMethod("add", i.result()))
							.output(o -> o.iterate(o.source()))
							.concrete(false)
							.condition(occurrences(between(0, null)))
							.dataType(AnnotatedWildcardTypes.unbounded(Annotations.from(Infer.class))))));

			listType = factory.apply("list", t -> t.baseModel(collectionType).dataType(new @Infer TypeToken<List<?>>() {}));

			setType = factory.apply("set", t -> t.baseModel(collectionType).dataType(new @Infer TypeToken<Set<?>>() {}));

			uriType = factory.apply("uri", t -> t.dataType(URI.class).node(n -> n.addChildBindingPoint(u -> u
					.name("uriString")
					.baseModel(primitive(Primitive.STRING))
					.input(i -> i.target().assign(
							invokeStatic(overType(URI.class).getConstructors().resolveOverload(String.class), i.result())))
					.output(o -> o.source().invokeResolvedMethod("toString")))));

			urlType = factory.apply("url", t -> t.dataType(URL.class).node(n -> n.addChildBindingPoint(u -> u
					.name("urlString")
					.baseModel(primitive(Primitive.STRING))
					.input(i -> i.target().assign(
							invokeStatic(overType(URL.class).getConstructors().resolveOverload(String.class), i.result())))
					.output(o -> o.source().invokeResolvedMethod("toString")))));

			bufferedDataType = factory.apply("bufferedData",
					t -> t.dataType(DataSource.class).node(n -> n
							.initializeInput(i -> i.provide(DataSource.class))

							.initializeOutput(o -> o.parent().invokeResolvedMethod("pipe", o.provide(DataTarget.class)))));

			bufferedDataItemType = factory
					.apply("bufferedDataItem",
							t -> t
									.dataType(new TypeToken<DataItem<?>>() {})
									.node(n -> n
											.initializeInput(i -> i.provide(DataSource.class).invokeResolvedMethod("get"))
											.initializeOutput(o -> o.provide(DataTarget.class).invokeResolvedMethod("put", o.parent()))));

			Model<Object> referenceBaseType = factory.apply("referenceBase", t -> t
					.<Object>dataType(overAnnotatedType(unbounded(from(Infer.class))))
					.concrete(false)
					.export(false)
					.node(n -> n
							.addChildBindingPoint(d -> d
									.dataType(new @Infer TypeToken<Model<?>>() {})
									.name("targetModel")
									.concrete(false)
									.valueResolution(ValueResolution.REGISTRATION_TIME)
									.noIO())
							.addChildBindingPoint(d -> d
									.baseModel(listType)
									.name("targetId")
									.concrete(false)
									.valueResolution(ValueResolution.REGISTRATION_TIME)
									.noIO()
									.node(o -> o.addChildBindingPoint(
											e -> e.baseModel(primitives.get(Primitive.QUALIFIED_NAME)).name("element"))))
							.addChildBindingPoint(d -> d
									.name("data")
									.baseModel(bufferedDataType)
									.input(i -> i.target().assign(i
											.provide(DereferenceSource.class)
											.invokeResolvedMethod("dereference", i.bound("targetModel"), i.bound("targetId"), i.result())))
									.output(o -> o.provide(ReferenceTarget.class).invokeResolvedMethod("reference",
											o.bound("targetModel"), o.bound("targetId"), o
													.source())))));

			referenceType = factory.apply("reference",
					t -> t
							.baseModel(referenceBaseType)
							.concrete(false)
							.node(n -> n.addChildBindingPoint(c -> c
									.name("targetModel")
									.baseModel(referenceBaseType)
									.concrete(false)
									.dataType(new @Infer TypeToken<Model<?>>() {})
									.node(o -> o
											.addChildBindingPoint(d -> d
													.name("targetModel")
													.baseModel(referenceBaseType)
													.dataType(new @Infer TypeToken<Model<?>>() {})
													.provideValue(new BufferingDataTarget()
															.put(Primitive.QUALIFIED_NAME, new QualifiedName("model", namespace))
															.buffer())
													.node(p -> p
															.addChildBindingPoint(e -> e
																	.name("targetModel")
																	.baseModel(referenceBaseType)
																	.concrete(false)
																	.dataType(new @Infer TypeToken<Model<?>>() {})
																	.provideValue(new BufferingDataTarget()
																			.put(Primitive.QUALIFIED_NAME, new QualifiedName("model", namespace))
																			.buffer()))
															.addChildBindingPoint(e -> e.name("targetId").provideValue(new BufferingDataTarget()
																	.put(Primitive.QUALIFIED_NAME, new QualifiedName("configurator", namespace))
																	.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace))
																	.buffer()))))
											.addChildBindingPoint(d -> d
													.name("targetId")
													.provideValue(new BufferingDataTarget()
															.put(Primitive.QUALIFIED_NAME, new QualifiedName("configurator", namespace))
															.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace))
															.buffer()))))));

			bindingReferenceType = factory
					.apply("bindingReference",
							t -> t
									.concrete(false)
									.<Object>dataType(overAnnotatedType(unbounded(from(Infer.class))))
									.node(
											n -> n
													.initializeInput(i -> i.provide(DereferenceSource.class))
													.addChildBindingPoint(c -> c.name("targetNode").baseModel(referenceType).noIO().node(o -> o
															.addChildBindingPoint(d -> d.name("targetModel").provideValue(new BufferingDataTarget()
																	.put(Primitive.QUALIFIED_NAME, new QualifiedName("binding", namespace))
																	.buffer()))
															.addChildBindingPoint(e -> e.name("targetId").provideValue(new BufferingDataTarget()
																	.put(Primitive.QUALIFIED_NAME, new QualifiedName("configurator", namespace))
																	.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace))
																	.buffer()))))));

			packageType = factory.apply("package",
					t -> t
							.dataType(new TypeToken<Package>() {})
							.node(n -> n.addChildBindingPoint(p -> p
									.baseModel(primitives.get(Primitive.STRING))
									.name("name")
									.input(i -> invokeResolvedStatic(Package.class, "getPackage"))
									.output(o -> o.source().invokeResolvedMethod("getName")))));

			typeType = factory.apply("type",
					t -> t
							.dataType(Type.class)
							.node(n -> n.addChildBindingPoint(p -> p
									.baseModel(primitives.get(Primitive.STRING))
									.name("name")
									.input(i -> invokeResolvedStatic(Types.class, "fromString", i.result()))
									.output(o -> invokeResolvedStatic(Types.class, "toString", o.source())))));

			classType = factory.apply("class", t -> t.baseModel(typeType).dataType(new TypeToken<Class<?>>() {}).node(
					n -> n.addChildBindingPoint(p -> p.name("name"))));

			annotatedTypeType = factory.apply("annotatedType",
					t -> t
							.dataType(AnnotatedType.class)
							.node(n -> n.addChildBindingPoint(p -> p
									.name("name")
									.baseModel(primitives.get(Primitive.STRING))
									.input(i -> invokeResolvedStatic(AnnotatedTypes.class, "fromString", i.result()))
									.output(o -> invokeResolvedStatic(AnnotatedTypes.class, "toString", o.source())))));

			typeTokenType = factory.apply("typeToken",
					t -> t
							.dataType(new TypeToken<TypeToken<?>>() {})
							.node(n -> n.addChildBindingPoint(c -> c
									.baseModel(annotatedTypeType)
									.input(i -> invokeResolvedStatic(TypeToken.class, "overAnnotatedType", i.result()))
									.output(o -> o.source().invokeResolvedMethod("getAnnotatedDeclaration")))));

			enumType = factory.apply("enum",
					t -> t
							.dataType(new TypeToken<Enum<?>>() {})
							.concrete(false)
							.node(n -> n
									.addChildBindingPoint(c -> c
											.dataType(new TypeToken<Class<? extends Enum<?>>>() {})
											.name("enumType")
											.noIO()
											.provideValue(new BufferingDataTarget().buffer())
											.node(p -> p.initializeInput(i -> i
													.provide(ProcessingContext.class)
													.invokeResolvedMethod("getBindingNode")
													.invokeResolvedMethod("dataType")
													.invokeResolvedMethod("getRawType"))))
									.addChildBindingPoint(p -> p
											.name("name")
											.baseModel(primitives.get(Primitive.STRING))
											.input(
													i -> invokeResolvedStatic(Enumeration.class, "valueOfEnum", i.bound("enumType"), i.result()))
											.output(o -> o.source().invokeResolvedMethod("getName")))));

			enumerationType = factory.apply("enumeration",
					t -> t
							.baseModel(enumerationBaseType)
							.concrete(false)
							.node(n -> n
									.addChildBindingPoint(c -> c
											.dataType(new TypeToken<Class<? extends Enumeration<?>>>() {})
											.name("enumType")
											.noIO()
											.provideValue(new BufferingDataTarget().buffer())
											.node(p -> p.initializeInput(i -> i
													.provide(ProcessingContext.class)
													.invokeResolvedMethod("getBindingNode")
													.invokeResolvedMethod("dataType")
													.invokeResolvedMethod("getRawType"))))
									.addChildBindingPoint(p -> p
											.name("name")
											.baseModel(primitives.get(Primitive.STRING))
											.input(i -> invokeResolvedStatic(Enumeration.class, "valueOf", i.bound("enumType"), i.result()))
											.output(o -> o.source().invokeResolvedMethod("getName")))));

			rangeType = factory.apply("range",
					t -> t
							.dataType(new TypeToken<Range<Integer>>() {})
							.node(n -> n.addChildBindingPoint(p -> p
									.baseModel(primitives.get(Primitive.STRING))
									.name("string")
									.input(i -> invokeResolvedStatic(Range.class, "parse", i.result()))
									.output(o -> invokeResolvedStatic(Range.class, "compose", o.source())))));

			includeType = factory
					.apply("include",
							t -> t.dataType(new @Infer TypeToken<Collection<?>>() {}).concrete(false).node(n -> n
									.initializeInput(i -> i.parent())
									.initializeOutput(o -> o.parent())
									.addChildBindingPoint(c -> c
											.name("targetModel")
											.baseModel(referenceType)
											.dataType(new TypeToken<Model<?>>() {})
											.concrete(false)
											.noIO()
											.valueResolution(ValueResolution.REGISTRATION_TIME)
											.node(e -> e
													.addChildBindingPoint(d -> d.name("targetModel").provideValue(new BufferingDataTarget()
															.put(Primitive.QUALIFIED_NAME, new QualifiedName("model", namespace))
															.buffer()))
													.addChildBindingPoint(d -> d.name("targetId").provideValue(new BufferingDataTarget()
															.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace))
															.buffer()))))
									.addChildBindingPoint(e -> e
											.name("object")
											.dataType(Collection.class)
											.input(i -> invokeResolvedStatic(IncludeTarget.class, "include", i.target()))
											.output(o -> invokeResolvedStatic(IncludeTarget.class, "include", o.bound("targetModel"))))));

			importType = factory
					.apply("import",
							t -> t.dataType(Object.class).concrete(false).node(n -> n
									.initializeInput(i -> i.parent())
									.initializeOutput(o -> o.parent())
									.addChildBindingPoint(c -> c
											.name("targetModel")
											.baseModel(referenceType)
											.dataType(new TypeToken<Model<?>>() {})
											.concrete(false)
											.noIO()
											.valueResolution(ValueResolution.REGISTRATION_TIME)
											.node(p -> p
													.addChildBindingPoint(d -> d.name("targetModel").provideValue(new BufferingDataTarget()
															.put(Primitive.QUALIFIED_NAME, new QualifiedName("model", namespace))
															.buffer()))
													.addChildBindingPoint(d -> d.name("targetId").provideValue(new BufferingDataTarget()
															.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace))
															.buffer()))))
									.addChildBindingPoint(d -> d
											.name("targetId")
											.baseModel(listType)
											.concrete(false)
											.valueResolution(ValueResolution.REGISTRATION_TIME)
											.noIO()
											.node(p -> p.addChildBindingPoint(
													e -> e.name("element").baseModel(primitives.get(Primitive.QUALIFIED_NAME)))))
									.addChildBindingPoint(d -> d
											.name("data")
											.baseModel(bufferedDataType)
											.input(i -> i.provide(ImportSource.class).invokeResolvedMethod("dereferenceImport",
													i.bound("targetModel"), i.bound("targetId"), i.result()))
											.output(o -> o.provide(ImportTarget.class).invokeResolvedMethod("referenceImport",
													o.bound("targetModel"), o.bound("targetId"), o
															.source())))));

			simpleModel = factory.apply("simpleModel",
					m -> m.dataType(overAnnotatedType(unbounded(from(Infer.class)))).concrete(false).node(
							n -> n.initializeInput(i -> i.parent()).initializeOutput(o -> o.parent()).addChildBindingPoint(w -> w
									.name("content")
									.concrete(false)
									.input(i -> i.target().assign(i.result()))
									.output(o -> o.source()))));

			/*
			 * Having trouble annotating Map.Entry for some reason, so need this
			 * kludge.
			 */
			AnnotatedType annotatedMapEntry = AnnotatedParameterizedTypes.from(
					AnnotatedTypes.over(Map.Entry.class, Annotations.from(Infer.class)),
					Arrays.asList(AnnotatedWildcardTypes.unbounded(), AnnotatedWildcardTypes.unbounded()));

			TypeToken<?> inferredMapEntry = overAnnotatedType(annotatedMapEntry);

			TypeToken<?> inferredMapEntrySet = overAnnotatedType(
					AnnotatedParameterizedTypes.from(AnnotatedTypes.over(Set.class, Annotations.from(Infer.class)),
							Arrays.asList(AnnotatedWildcardTypes.upperBounded(annotatedMapEntry))));

			mapModel = factory.apply("map",
					c -> c
							.dataType(new @Infer TypeToken<Map<?, ?>>() {})
							.node(n -> n.addChildBindingPoint(f -> f
									.name("entry")
									.condition(occurrences(between(0, null)))
									.noInput()
									.output(o -> o.iterate(o.source().invokeResolvedMethod("keySet")))
									.dataType(overAnnotatedType(unbounded(from(Infer.class))))
									.node(p -> p
											.addChildBindingPoint(k -> k
													.name("key")
													.noInput()
													.output(o -> o.source())
													.dataType(AnnotatedWildcardTypes.unbounded(Annotations.from(Infer.class)))
													.extensible(true))
											.addChildBindingPoint(v -> v
													.name("value")
													.output(o -> o.s)
													.dataType(AnnotatedWildcardTypes.unbounded(Annotations.from(Infer.class)))
													.extensible(true))))));
		}

		@Override
		public Model<Package> packageType() {
			return packageType;
		}

		@Override
		public Model<Class<?>> classType() {
			return classType;
		}

		@Override
		public Model<Type> typeType() {
			return typeType;
		}

		@Override
		public Model<AnnotatedType> annotatedTypeType() {
			return annotatedTypeType;
		}

		@Override
		public Model<TypeToken<?>> typeTokenType() {
			return typeTokenType;
		}

		@Override
		public Model<Enum<?>> enumType() {
			return enumType;
		}

		@Override
		public Model<Enumeration<?>> enumerationType() {
			return enumerationType;
		}

		@Override
		public Model<Range<Integer>> rangeType() {
			return rangeType;
		}

		@Override
		public Model<Object> referenceType() {
			return referenceType;
		}

		@Override
		public Model<Object> bindingReferenceType() {
			return bindingReferenceType;
		}

		@Override
		public Model<DataSource> bufferedDataType() {
			return bufferedDataType;
		}

		@Override
		public Model<DataItem<?>> bufferedDataItemType() {
			return bufferedDataItemType;
		}

		@Override
		public Model<Object[]> arrayType() {
			return arrayType;
		}

		@Override
		public Model<Collection<?>> collectionType() {
			return collectionType;
		}

		@Override
		public Model<List<?>> listType() {
			return listType;
		}

		@Override
		public Model<Set<?>> setType() {
			return setType;
		}

		@Override
		public Model<Collection<?>> includeType() {
			return includeType;
		}

		@Override
		public Model<Object> importType() {
			return importType;
		}

		@Override
		public Model<URI> uriType() {
			return uriType;
		}

		@Override
		public Model<URL> urlType() {
			return urlType;
		}

		@Override
		public Model<?> simpleModel() {
			return simpleModel;
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

		ModelFactory modelFactory = new ModelFactory() {
			@Override
			public <T> Model<T> apply(String name, Function<ModelConfigurator<?>, ModelConfigurator<T>> modelFunction) {
				return modelFunction.apply(schemaConfigurator.addModel().name(name, namespace)).create();
			}
		};

		Model<Enumeration<?>> enumerationBaseType = modelFactory.apply("enumerationBase",
				c -> c.concrete(false).export(false).dataType(new @Infer TypeToken<Enumeration<?>>() {}));

		Model<?> primitive = modelFactory.apply("primitive",
				p -> p
						.concrete(false)
						.export(false)
						.node(n -> n
								.addChildBindingPoint(c -> c
										.name("dataType")
										.concrete(false)
										.baseModel(enumerationBaseType)
										.valueResolution(ValueResolution.REGISTRATION_TIME)
										.dataType(new @Infer TypeToken<Primitive<?>>() {})
										.noIO())
								.addChildBindingPoint(c -> c
										.name("io")
										.dataType(void.class)
										.input(i -> i
												.target()
												.assign(i.provide(DataSource.class).invokeResolvedMethod("get", i.bound("dataType"))))
										.output(o -> o.provide(DataTarget.class).invokeResolvedMethod("put", o.bound("dataType"),
												o.source())))));

		primitives = new HashMap<>();
		for (Primitive<?> dataType : Enumeration.getConstants(Primitive.class)) {
			primitives.put(dataType,
					modelFactory.apply(dataType.name(),
							p -> p
									.baseModel(primitive)
									.dataType(dataType.dataClass())
									.node(n -> n.addChildBindingPoint(
											c -> c.name("dataType").dataType(resolvePrimitiveDataType(dataType)).provideValue(
													new BufferingDataTarget().put(Primitive.STRING, dataType.name()).buffer())))));
		}

		derivedTypes = new DerivedImpl(modelFactory, enumerationBaseType);

		/*
		 * Schema
		 */
		baseSchema = schemaConfigurator.create();
	}

	private <T> TypeToken<Primitive<T>> resolvePrimitiveDataType(Primitive<T> dataType) {
		return new TypeToken<Primitive<T>>() {}.withTypeArgument(new TypeParameter<T>() {},
				Types.wrapPrimitive(dataType.dataClass()));
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
