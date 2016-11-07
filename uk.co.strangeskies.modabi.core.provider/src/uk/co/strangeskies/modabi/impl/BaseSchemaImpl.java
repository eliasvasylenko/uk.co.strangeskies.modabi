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
import static uk.co.strangeskies.reflection.AnnotatedWildcardTypes.unbounded;
import static uk.co.strangeskies.reflection.Annotations.from;
import static uk.co.strangeskies.reflection.codegen.InvocationExpression.invokeResolvedStatic;
import static uk.co.strangeskies.reflection.codegen.InvocationExpression.invokeStatic;
import static uk.co.strangeskies.reflection.token.TypeToken.overAnnotatedType;
import static uk.co.strangeskies.reflection.token.TypeToken.overType;

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
import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.AnnotatedWildcardTypes;
import uk.co.strangeskies.reflection.Annotations;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.Types;
import uk.co.strangeskies.reflection.token.TypeParameter;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypeToken.Infer;
import uk.co.strangeskies.utilities.Enumeration;

public class BaseSchemaImpl implements BaseSchema {
	private interface ModelFactory {
		<T> Model<T> apply(String name, Function<ModelConfigurator<?>, ModelConfigurator<T>> type);
	}

	private class DerivedImpl implements Derived {
		private final Model<?> referenceModel;
		private final Model<?> bindingReferenceModel;
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

		public DerivedImpl(ModelFactory factory, Model<Enumeration<?>> enumerationBaseType) {
			Namespace namespace = BaseSchema.QUALIFIED_NAME.getNamespace();

			arrayModel = factory.apply("array",
					t -> t
							.dataType(new @Infer TypeToken<Object[]>() {})
							.node(n -> n
									.initializeInput(i -> i.provide(new TypeToken<List<?>>() {}))
									.initializeOutput(o -> invokeResolvedStatic(Arrays.class, "asList", o.parent()))
									.addChildBindingPoint(c -> c
											.name("element")
											.input(i -> i.target().invokeResolvedMethod("add", i.result()))
											.output(o -> o.iterate(o.source()))
											.concrete(false)
											.dataType(AnnotatedWildcardTypes.unbounded(Annotations.from(Infer.class)))
											.bindingCondition(occurrences(between(0, null))))
									.addChildBindingPoint(c -> c
											.name("toArray")
											.dataType(void.class)
											.input(i -> i.target().assign(i.target().invokeResolvedMethod("toArray")))
											.output(IOConfigurator::none))));

			collectionModel = factory.apply("collection",
					t -> t
							.dataType(new @Infer TypeToken<Collection<?>>() {})
							.node(n -> n
									.initializeInput(i -> i.provide())
									.addChildBindingPoint(c -> c
											.name("element")
											.input(i -> i.target().invokeResolvedMethod("add", i.result()))
											.output(o -> o.iterate(o.source()))
											.concrete(false)
											.bindingCondition(occurrences(between(0, null)))
											.dataType(AnnotatedWildcardTypes.unbounded(Annotations.from(Infer.class))))));

			listModel = factory.apply("list", t -> t.baseModel(collectionModel).dataType(new @Infer TypeToken<List<?>>() {}));

			setModel = factory.apply("set", t -> t.baseModel(collectionModel).dataType(new @Infer TypeToken<Set<?>>() {}));

			uriModel = factory.apply("uri", t -> t.dataType(URI.class).node(n -> n.addChildBindingPoint(u -> u
					.name("uriString")
					.baseModel(primitive(Primitive.STRING))
					.input(i -> i.target().assign(
							invokeStatic(overType(URI.class).getConstructors().resolveOverload(String.class), i.result())))
					.output(o -> o.source().invokeResolvedMethod("toString")))));

			urlModel = factory.apply("url", t -> t.dataType(URL.class).node(n -> n.addChildBindingPoint(u -> u
					.name("urlString")
					.baseModel(primitive(Primitive.STRING))
					.input(i -> i.target().assign(
							invokeStatic(overType(URL.class).getConstructors().resolveOverload(String.class), i.result())))
					.output(o -> o.source().invokeResolvedMethod("toString")))));

			bufferedDataModel = factory.apply("bufferedData",
					t -> t.dataType(DataSource.class).node(n -> n
							.initializeInput(i -> i.provide(DataSource.class))

							.initializeOutput(o -> o.parent().invokeResolvedMethod("pipe", o.provide(DataTarget.class)))));

			bufferedDataItemModel = factory
					.apply("bufferedDataItem",
							t -> t
									.dataType(new TypeToken<DataItem<?>>() {})
									.node(n -> n
											.initializeInput(i -> i.provide(DataSource.class).invokeResolvedMethod("get"))
											.initializeOutput(o -> o.provide(DataTarget.class).invokeResolvedMethod("put", o.parent()))));

			Model<?> referenceBaseModel = factory.apply("referenceBase",
					t -> t.dataType(overAnnotatedType(unbounded(from(Infer.class)))).concrete(false).export(false).node(n -> n
							.addChildBindingPoint(d -> d
									.dataType(new @Infer TypeToken<Model<?>>() {})
									.name("targetModel")
									.concrete(false)
									.valueResolution(ValueResolution.DECLARATION_TIME)
									.input(IOConfigurator::none)
									.output(IOConfigurator::none))
							.addChildBindingPoint(d -> d
									.baseModel(listModel)
									.name("targetId")
									.concrete(false)
									.valueResolution(ValueResolution.DECLARATION_TIME)
									.input(IOConfigurator::none)
									.output(IOConfigurator::none)
									.node(o -> o.addChildBindingPoint(
											e -> e.baseModel(primitives.get(Primitive.QUALIFIED_NAME)).name("element"))))
							.addChildBindingPoint(d -> d
									.name("data")
									.baseModel(bufferedDataModel)
									.input(i -> i.target().assign(i
											.provide(DereferenceSource.class)
											.invokeResolvedMethod("dereference", i.bound("targetModel"), i.bound("targetId"), i.result())))
									.output(o -> o.provide(ReferenceTarget.class).invokeResolvedMethod("reference",
											o.bound("targetModel"), o.bound("targetId"), o
													.source())))));

			referenceModel = factory.apply("reference",
					t -> t
							.baseModel(referenceBaseModel)
							.concrete(false)
							.node(n -> n.addChildBindingPoint(c -> c
									.name("targetModel")
									.baseModel(referenceBaseModel)
									.concrete(false)
									.dataType(new @Infer TypeToken<Model<?>>() {})
									.node(o -> o
											.addChildBindingPoint(d -> d
													.name("targetModel")
													.baseModel(referenceBaseModel)
													.dataType(new @Infer TypeToken<Model<?>>() {})
													.provideValue(new BufferingDataTarget()
															.put(Primitive.QUALIFIED_NAME, new QualifiedName("model", namespace))
															.buffer())
													.node(p -> p
															.addChildBindingPoint(e -> e
																	.name("targetModel")
																	.baseModel(referenceBaseModel)
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

			bindingReferenceModel = factory.apply("bindingReference",
					t -> t.concrete(false).dataType(overAnnotatedType(unbounded(from(Infer.class)))).node(
							n -> n.initializeInput(i -> i.provide(DereferenceSource.class)).addChildBindingPoint(c -> c
									.name("targetNode")
									.baseModel(referenceModel)
									.input(IOConfigurator::none)
									.output(IOConfigurator::none)
									.node(o -> o
											.addChildBindingPoint(d -> d.name("targetModel").provideValue(new BufferingDataTarget()
													.put(Primitive.QUALIFIED_NAME, new QualifiedName("binding", namespace))
													.buffer()))
											.addChildBindingPoint(e -> e.name("targetId").provideValue(new BufferingDataTarget()
													.put(Primitive.QUALIFIED_NAME, new QualifiedName("configurator", namespace))
													.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace))
													.buffer()))))));

			packageModel = factory.apply("package",
					t -> t
							.dataType(new TypeToken<Package>() {})
							.node(n -> n.addChildBindingPoint(p -> p
									.baseModel(primitives.get(Primitive.STRING))
									.name("name")
									.input(i -> invokeResolvedStatic(Package.class, "getPackage"))
									.output(o -> o.source().invokeResolvedMethod("getName")))));

			typeModel = factory.apply("type",
					t -> t
							.dataType(Type.class)
							.node(n -> n.addChildBindingPoint(p -> p
									.baseModel(primitives.get(Primitive.STRING))
									.name("name")
									.input(i -> invokeResolvedStatic(Types.class, "fromString", i.result()))
									.output(o -> invokeResolvedStatic(Types.class, "toString", o.source())))));

			classModel = factory.apply("class", t -> t.baseModel(typeModel).dataType(new TypeToken<Class<?>>() {}).node(
					n -> n.addChildBindingPoint(p -> p.name("name"))));

			annotatedTypeModel = factory.apply("annotatedType",
					t -> t
							.dataType(AnnotatedType.class)
							.node(n -> n.addChildBindingPoint(p -> p
									.name("name")
									.baseModel(primitives.get(Primitive.STRING))
									.input(i -> invokeResolvedStatic(AnnotatedTypes.class, "fromString", i.result()))
									.output(o -> invokeResolvedStatic(AnnotatedTypes.class, "toString", o.source())))));

			typeTokenModel = factory.apply("typeToken",
					t -> t
							.dataType(new TypeToken<TypeToken<?>>() {})
							.node(n -> n.addChildBindingPoint(c -> c
									.baseModel(annotatedTypeModel)
									.input(i -> invokeResolvedStatic(TypeToken.class, "overAnnotatedType", i.result()))
									.output(o -> o.source().invokeResolvedMethod("getAnnotatedDeclaration")))));

			enumModel = factory.apply("enum",
					t -> t
							.dataType(new TypeToken<Enum<?>>() {})
							.concrete(false)
							.node(n -> n
									.addChildBindingPoint(c -> c
											.dataType(new TypeToken<Class<? extends Enum<?>>>() {})
											.name("enumType")
											.input(IOConfigurator::none)
											.output(IOConfigurator::none)
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

			enumerationModel = factory.apply("enumeration",
					t -> t
							.baseModel(enumerationBaseType)
							.concrete(false)
							.node(n -> n
									.addChildBindingPoint(c -> c
											.dataType(new TypeToken<Class<? extends Enumeration<?>>>() {})
											.name("enumType")
											.input(IOConfigurator::none)
											.output(IOConfigurator::none)
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

			rangeModel = factory.apply("range",
					t -> t
							.dataType(new TypeToken<Range<Integer>>() {})
							.node(n -> n.addChildBindingPoint(p -> p
									.baseModel(primitives.get(Primitive.STRING))
									.name("string")
									.input(i -> invokeResolvedStatic(Range.class, "parse", i.result()))
									.output(o -> invokeResolvedStatic(Range.class, "compose", o.source())))));

			includeModel = factory
					.apply("include",
							t -> t.dataType(new @Infer TypeToken<Collection<?>>() {}).concrete(false).node(n -> n
									.initializeInput(i -> i.parent())
									.initializeOutput(o -> o.parent())
									.addChildBindingPoint(c -> c
											.name("targetModel")
											.baseModel(referenceModel)
											.dataType(new TypeToken<Model<?>>() {})
											.concrete(false)
											.input(IOConfigurator::none)
											.output(IOConfigurator::none)
											.valueResolution(ValueResolution.DECLARATION_TIME)
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

			importModel = factory
					.apply("import",
							t -> t.dataType(Object.class).concrete(false).node(n -> n
									.initializeInput(i -> i.parent())
									.initializeOutput(o -> o.parent())
									.addChildBindingPoint(c -> c
											.name("targetModel")
											.baseModel(referenceModel)
											.dataType(new TypeToken<Model<?>>() {})
											.concrete(false)
											.input(IOConfigurator::none)
											.output(IOConfigurator::none)
											.valueResolution(ValueResolution.DECLARATION_TIME)
											.node(p -> p
													.addChildBindingPoint(d -> d.name("targetModel").provideValue(new BufferingDataTarget()
															.put(Primitive.QUALIFIED_NAME, new QualifiedName("model", namespace))
															.buffer()))
													.addChildBindingPoint(d -> d.name("targetId").provideValue(new BufferingDataTarget()
															.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace))
															.buffer()))))
									.addChildBindingPoint(d -> d
											.name("targetId")
											.baseModel(listModel)
											.concrete(false)
											.valueResolution(ValueResolution.DECLARATION_TIME)
											.input(IOConfigurator::none)
											.output(IOConfigurator::none)
											.node(p -> p.addChildBindingPoint(
													e -> e.name("element").baseModel(primitives.get(Primitive.QUALIFIED_NAME)))))
									.addChildBindingPoint(d -> d
											.name("data")
											.baseModel(bufferedDataModel)
											.input(i -> i.provide(ImportSource.class).invokeResolvedMethod("dereferenceImport",
													i.bound("targetModel"), i.bound("targetId"), i.result()))
											.output(o -> o.provide(ImportTarget.class).invokeResolvedMethod("referenceImport",
													o.bound("targetModel"), o.bound("targetId"), o
															.source())))));

			/*
			 * Having trouble annotating Map.Entry for some reason, so need this
			 * kludge.
			 */
			mapModel = factory.apply("map",
					c -> c
							.dataType(new @Infer TypeToken<Map<?, ?>>() {})
							.node(n -> n.addChildBindingPoint(f -> f
									.name("entry")
									.bindingCondition(occurrences(between(0, null)))
									.input(IOConfigurator::none)
									.output(o -> o.iterate(o.source().invokeResolvedMethod("entrySet")))
									.dataType(void.class)
									.node(p -> p
											.initializeInput(i -> i.parent())
											.addChildBindingPoint(k -> k
													.name("key")
													.input(IOConfigurator::none)
													.output(o -> o.source().invokeResolvedMethod("getKey"))
													.dataType(AnnotatedWildcardTypes.unbounded(Annotations.from(Infer.class)))
													.extensible(true))
											.addChildBindingPoint(v -> v
													.name("value")
													.output(o -> o.source().invokeResolvedMethod("getValue"))
													.input(i -> i.target().invokeResolvedMethod("put", i.bound("key"), i.result()))
													.dataType(AnnotatedWildcardTypes.unbounded(Annotations.from(Infer.class)))
													.extensible(true))))));
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

		ModelFactory modelFactory = new ModelFactory() {
			@Override
			public <T> Model<T> apply(String name, Function<ModelConfigurator<?>, ModelConfigurator<T>> modelFunction) {
				return modelFunction.apply(schemaConfigurator.addModel().name(name, namespace)).create();
			}
		};

		Model<Enumeration<?>> enumerationBaseType = modelFactory.apply("enumerationBase",
				c -> c.concrete(false).export(false).dataType(new @Infer TypeToken<Enumeration<?>>() {}));

		Model<?> primitive = modelFactory.apply("primitive", p -> p.concrete(false).export(false).node(n -> n
				.addChildBindingPoint(c -> c
						.name("dataType")
						.concrete(false)
						.baseModel(enumerationBaseType)
						.valueResolution(ValueResolution.DECLARATION_TIME)
						.dataType(new @Infer TypeToken<Primitive<?>>() {})
						.input(IOConfigurator::none)
						.output(IOConfigurator::none))
				.addChildBindingPoint(c -> c
						.name("io")
						.dataType(void.class)
						.input(i -> i.target().assign(i.provide(DataSource.class).invokeResolvedMethod("get", i.bound("dataType"))))
						.output(o -> o.provide(DataTarget.class).invokeResolvedMethod("put", o.bound("dataType"), o.source())))));

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
