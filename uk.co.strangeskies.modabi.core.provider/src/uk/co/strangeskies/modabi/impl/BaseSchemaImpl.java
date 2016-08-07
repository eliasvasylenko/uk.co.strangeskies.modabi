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

import static uk.co.strangeskies.reflection.AnnotatedWildcardTypes.unbounded;
import static uk.co.strangeskies.reflection.Annotations.from;
import static uk.co.strangeskies.reflection.TypeToken.over;

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
import uk.co.strangeskies.modabi.DataTypes;
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
import uk.co.strangeskies.modabi.processing.InputBindingStrategy;
import uk.co.strangeskies.modabi.processing.OutputBindingStrategy;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.providers.DereferenceSource;
import uk.co.strangeskies.modabi.processing.providers.ImportSource;
import uk.co.strangeskies.modabi.processing.providers.ImportTarget;
import uk.co.strangeskies.modabi.processing.providers.IncludeTarget;
import uk.co.strangeskies.modabi.processing.providers.ReferenceTarget;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataNode.Format;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.DataTypeConfigurator;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
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
	private interface TypeFactory {
		<T> DataType<T> apply(String name, Function<DataTypeConfigurator<Object>, DataTypeConfigurator<T>> type);
	}

	private interface ModelFactory {
		<T> Model<T> apply(String name, Function<ModelConfigurator<Object>, ModelConfigurator<T>> type);
	}

	private class DerivedTypesImpl implements DerivedTypes {
		private final DataType<Object> referenceType;
		private final DataType<Object> bindingReferenceType;
		private final DataType<DataSource> bufferedDataType;
		private final DataType<DataItem<?>> bufferedDataItemType;

		private final DataType<Package> packageType;
		private final DataType<Class<?>> classType;
		private final DataType<Type> typeType;
		private final DataType<AnnotatedType> annotatedTypeType;
		private final DataType<TypeToken<?>> typeTokenType;
		private final DataType<Enum<?>> enumType;
		private final DataType<Enumeration<?>> enumerationType;
		private final DataType<Range<Integer>> rangeType;
		private final DataType<Object[]> arrayType;
		private final DataType<Collection<?>> collectionType;
		private final DataType<List<?>> listType;
		private final DataType<Set<?>> setType;
		private final DataType<Object> importType;
		private final DataType<Collection<?>> includeType;
		private final DataType<URI> uriType;
		private final DataType<URL> urlType;

		public DerivedTypesImpl(TypeFactory factory, DataType<Enumeration<?>> enumerationBaseType) {
			Namespace namespace = BaseSchema.QUALIFIED_NAME.getNamespace();

			arrayType = factory.apply("array",
					t -> t.dataType(new @Infer TypeToken<Object[]>() {}).concrete(false)
							.inputBindingStrategy(InputBindingStrategy.PROVIDED).inputBindingType(List.class)
							.outputBindingStrategy(OutputBindingStrategy.STATIC_FACTORY)
							.outputBindingType(new @Infer TypeToken<List<?>>() {}).outputBindingFactoryType(Arrays.class)
							.outputBindingMethod("asList")
							.addChild(c -> c.data().name("element").inputMethod("add").outputSelf().concrete(false)
									.occurrences(Range.between(0, null)).chainedInput(false))
							.addChild(c -> c.inputSequence().name("toArray").chainedInput(true).castInput(true)));

			collectionType = factory.apply("collection",
					t -> t.dataType(new @Infer TypeToken<Collection<?>>() {}).inputBindingStrategy(InputBindingStrategy.PROVIDED)
							.outputBindingStrategy(OutputBindingStrategy.SIMPLE)
							.addChild(c -> c.data().name("element").inputMethod("add").outputSelf().synchronous(true).concrete(false)
									.extensible(true).occurrences(Range.between(0, null))));

			listType = factory.apply("list", t -> t.dataType(new @Infer TypeToken<List<?>>() {}).baseType(collectionType));

			setType = factory.apply("set", t -> t.dataType(new @Infer TypeToken<Set<?>>() {}).baseType(collectionType));

			uriType = factory.apply("uri", t -> t.dataType(URI.class).inputBindingStrategy(InputBindingStrategy.CONSTRUCTOR)
					.addChild(u -> u.data().name("uriString").type(primitiveType(Primitive.STRING)).outputMethod("toString")));

			urlType = factory.apply("url", t -> t.dataType(URL.class).inputBindingStrategy(InputBindingStrategy.CONSTRUCTOR)
					.addChild(u -> u.data().name("urlString").type(primitiveType(Primitive.STRING)).outputMethod("toString")));

			bufferedDataType = factory.apply("bufferedData",
					t -> t.dataType(DataSource.class).inputBindingType(DataSource.class)
							.inputBindingStrategy(InputBindingStrategy.PROVIDED).outputBindingType(DataTarget.class)
							.outputBindingStrategy(OutputBindingStrategy.ACCEPT_PROVIDED).outputBindingMethod("pipe"));

			bufferedDataItemType = factory.apply("bufferedDataItem",
					t -> t.dataType(new TypeToken<DataItem<?>>() {}).inputBindingType(DataSource.class)
							.inputBindingStrategy(InputBindingStrategy.PROVIDED).outputBindingType(DataTarget.class)
							.outputBindingStrategy(OutputBindingStrategy.PASS_TO_PROVIDED).outputBindingMethod("put")
							.addChild(c -> c.inputSequence().name("get").chainedInput(true)));

			DataType<Object> referenceBaseType = factory.apply("referenceBase",
					t -> t.<Object>dataType(TypeToken.over(AnnotatedWildcardTypes.unbounded(Annotations.from(Infer.class))))
							.concrete(false).export(false).inputBindingType(DereferenceSource.class)
							.inputBindingStrategy(InputBindingStrategy.PROVIDED).outputBindingFactoryType(ReferenceTarget.class)
							.outputBindingType(DataSource.class).outputBindingMethod("reference").outputBindingMethodUnchecked(true)
							.outputBindingStrategy(OutputBindingStrategy.PROVIDED_FACTORY)
							.providedOutputBindingMethodParameters("targetModel", "targetId", "this")
							.addChild(d -> d.data().dataType(new @Infer TypeToken<Model<?>>() {}).name("targetModel").concrete(false)
									.valueResolution(ValueResolution.REGISTRATION_TIME).inputMethod("dereference").chainedInput(true)
									.outputNone().postInputType(new @Infer TypeToken<Function<QualifiedName, ?>>() {}))
							.addChild(d -> d.data().type(primitives.get(Primitive.QUALIFIED_NAME)).name("targetId")
									.concrete(false).valueResolution(ValueResolution.REGISTRATION_TIME).inputMethod("apply")
									.chainedInput(true).outputNone().postInputType(new @Infer TypeToken<Function<DataSource, ?>>() {}))
							.addChild(d -> d.data().name("data").type(bufferedDataType).inputMethod("apply").chainedInput(true)
									.outputSelf()));

			referenceType = factory
					.apply("reference",
							t -> t.baseType(referenceBaseType).concrete(false)
									.addChild(
											c -> c.data().name("targetModel").type(referenceBaseType).concrete(false)
													.dataType(new @Infer TypeToken<Model<?>>() {})
													.addChild(d -> d.data().name("targetModel").type(referenceBaseType).extensible(true)
															.dataType(new @Infer TypeToken<Model<?>>() {}).provideValue(new BufferingDataTarget()
																	.put(Primitive.QUALIFIED_NAME, new QualifiedName("model", namespace)).buffer())
															.addChild(e -> e.data().name("targetModel").type(referenceBaseType).extensible(true)
																	.concrete(false).dataType(new @Infer TypeToken<Model<?>>() {}).provideValue(
																			new BufferingDataTarget()
																					.put(Primitive.QUALIFIED_NAME, new QualifiedName("model", namespace))
																					.buffer()))
															.addChild(e -> e.data().name("targetId")
																	.provideValue(new BufferingDataTarget()
																			.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace)).buffer())))
													.addChild(d -> d.data().name("targetId").provideValue(new BufferingDataTarget()
															.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace)).buffer()))));

			bindingReferenceType = factory.apply("bindingReference",
					t -> t.concrete(false).inputBindingType(DereferenceSource.class)
							.<Object>dataType(over(unbounded(from(Infer.class)))).inputBindingStrategy(InputBindingStrategy.PROVIDED)
							.addChild(c -> c.data().name("targetNode").type(referenceType).outputNone().inputMethod("dereference")
									.addChild(d -> d.data().name("targetModel")
											.provideValue(new BufferingDataTarget()
													.put(Primitive.QUALIFIED_NAME, new QualifiedName("binding", namespace)).buffer()))
									.addChild(e -> e.data().name("targetId").provideValue(new BufferingDataTarget()
											.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace)).buffer()))));

			packageType = factory.apply("package",
					t -> t.dataType(new TypeToken<Package>() {}).inputBindingStrategy(InputBindingStrategy.STATIC_FACTORY)
							.addChild(p -> p.data().type(primitives.get(Primitive.STRING)).name("name").inputMethod("getPackage")));

			typeType = factory.apply("type",
					t -> t.dataType(Type.class).inputBindingStrategy(InputBindingStrategy.STATIC_FACTORY)
							.inputBindingType(Types.class).outputBindingStrategy(OutputBindingStrategy.STATIC_FACTORY)
							.outputBindingType(String.class).outputBindingFactoryType(Types.class).outputBindingMethod("toString")
							.addChild(p -> p.data().type(primitives.get(Primitive.STRING)).name("name").inputMethod("fromString")
									.outputSelf()));

			classType = factory.apply("class", t -> t.baseType(typeType).dataType(new TypeToken<Class<?>>() {})
					.addChild(p -> p.data().name("name").postInputType(new TypeToken<Class<?>>() {}).castInput(true)));

			annotatedTypeType = factory.apply("annotatedType",
					t -> t.dataType(AnnotatedType.class).inputBindingStrategy(InputBindingStrategy.STATIC_FACTORY)
							.inputBindingType(AnnotatedTypes.class).addChild(p -> p.data().type(primitives.get(Primitive.STRING))
									.name("name").inputMethod("fromString").outputMethod("toString")));

			typeTokenType = factory.apply("typeToken",
					t -> t.dataType(new TypeToken<TypeToken<?>>() {}).inputBindingStrategy(InputBindingStrategy.STATIC_FACTORY)
							.addChild(
									o -> o.data().type(annotatedTypeType).outputMethod("getAnnotatedDeclaration").inputMethod("over")));

			enumType = factory.apply("enum",
					t -> t.dataType(new TypeToken<Enum<?>>() {}).inputBindingType(Enumeration.class).concrete(false)
							.inputBindingStrategy(InputBindingStrategy.STATIC_FACTORY)
							.outputBindingStrategy(OutputBindingStrategy.SIMPLE)
							.addChild(n -> n.inputSequence().name("valueOfEnum")
									.addChild(o -> o.data().dataType(new TypeToken<Class<? extends Enum<?>>>() {}).name("enumType")
											.outputNone().provideValue(new BufferingDataTarget().buffer())
											.inputBindingStrategy(InputBindingStrategy.PROVIDED).inputBindingType(ProcessingContext.class)
											.addChild(e -> e.inputSequence().name("bindingNode").inputMethod("getBindingNode")
													.chainedInput(true).postInputType(new TypeToken<DataType<?>>() {}).castInput(true))
											.addChild(p -> p.inputSequence().name("dataType").chainedInput(true))
											.addChild(p -> p.inputSequence().name("getRawType").chainedInput(true).castInput(true)
													.postInputType(new TypeToken<Class<? extends Enum<?>>>() {})))
									.addChild(o -> o.data().name("name").type(primitives.get(Primitive.STRING)))));

			enumerationType = factory.apply("enumeration",
					t -> t.baseType(enumerationBaseType).concrete(false)
							.addChild(n -> n.inputSequence().name("valueOf").inputMethod("valueOf")
									.addChild(o -> o.data().dataType(new TypeToken<Class<? extends Enumeration<?>>>() {})
											.name("enumerationType").outputNone().provideValue(new BufferingDataTarget().buffer())
											.inputBindingStrategy(InputBindingStrategy.PROVIDED).inputBindingType(ProcessingContext.class)
											.addChild(e -> e.inputSequence().name("bindingNode").inputMethod("getBindingNode")
													.chainedInput(true).postInputType(new TypeToken<DataType<?>>() {}).castInput(true))
											.addChild(p -> p.inputSequence().name("dataType").chainedInput(true))
											.addChild(p -> p.inputSequence().name("getRawType").chainedInput(true).castInput(true)
													.postInputType(new TypeToken<Class<? extends Enumeration<?>>>() {})))
									.addChild(o -> o.data().name("name").type(primitives.get(Primitive.STRING)))));

			rangeType = factory.apply("range",
					t -> t.dataType(new TypeToken<Range<Integer>>() {}).inputBindingStrategy(InputBindingStrategy.STATIC_FACTORY)
							.outputBindingStrategy(OutputBindingStrategy.STATIC_FACTORY).outputBindingType(String.class)
							.outputBindingFactoryType(Range.class)
							.addChild(p -> p.data().type(primitives.get(Primitive.STRING)).outputSelf().name("string")));

			includeType = factory.apply("include",
					t -> t.dataType(new @Infer TypeToken<Collection<?>>() {}).outputBindingType(IncludeTarget.class)
							.inputBindingStrategy(InputBindingStrategy.TARGET_ADAPTOR)
							.outputBindingStrategy(OutputBindingStrategy.PASS_TO_PROVIDED).outputBindingMethod("include")
							.providedOutputBindingMethodParameters("targetModel", "this").outputBindingMethodUnchecked(true)
							.concrete(false)
							.addChild(c -> c.data().name("targetModel").type(referenceType).dataType(new TypeToken<Model<?>>() {})
									.concrete(false).outputNone().inputNone().valueResolution(ValueResolution.REGISTRATION_TIME)
									.addChild(d -> d.data().name("targetModel")
											.provideValue(new BufferingDataTarget()
													.put(Primitive.QUALIFIED_NAME, new QualifiedName("model", namespace)).buffer()))
									.addChild(d -> d.data().name("targetId")
											.provideValue(new BufferingDataTarget()
													.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace)).buffer())))
							.addChild(c -> c.data().name("in").outputNone().inputNone()
									.inputBindingStrategy(InputBindingStrategy.PROVIDED).dataType(IncludeTarget.class)
									.addChild(d -> d.inputSequence().name("include").inputMethod("include").uncheckedInput(true)
											.addChild(e -> e.data()
													.dataType(new TypeToken<Model<?>>() {})
													.name("targetModel").outputNone()
													.inputBindingStrategy(InputBindingStrategy.PROVIDED).inputBindingType(ProcessingContext.class)
													.addChild(f -> f.data().name("bindingNode").inputMethod("getBindingNode")
															.type(primitives.get(Primitive.INT)).outputNone().chainedInput(true).provideValue(
																	new BufferingDataTarget().put(Primitive.INT, 2).buffer()))
													.addChild(f -> f.data().name("child").type(primitives.get(Primitive.QUALIFIED_NAME))
															.chainedInput(true).outputNone()
															.provideValue(new BufferingDataTarget()
																	.put(Primitive.QUALIFIED_NAME, new QualifiedName("targetModel", namespace)).buffer())
															.postInputType(new TypeToken<DataNode<?>>() {}).castInput(true))
													.addChild(f -> f.inputSequence().name("providedValue").chainedInput(true).castInput(true)
															.postInputType(new TypeToken<Model<?>>() {})))
											.addChild(e -> e.data().name("object").dataType(Collection.class).outputNone()
													.inputBindingStrategy(InputBindingStrategy.PROVIDED).inputBindingType(
															ProcessingContext.class)
													.addChild(f -> f.data().name("bindingObject").inputMethod("getBindingObject")
															.type(primitives.get(Primitive.INT)).chainedInput(true).outputNone()
															.provideValue(new BufferingDataTarget().put(Primitive.INT, 1).buffer()))
													.addChild(f -> f.inputSequence().name("getObject").chainedInput(true))))));

			importType = factory.apply("import",
					t -> t.dataType(Object.class).concrete(false).inputBindingStrategy(InputBindingStrategy.SOURCE_ADAPTOR)
							.outputBindingStrategy(OutputBindingStrategy.SIMPLE).outputBindingMethod("this")
							.addChild(b -> b.data().name("import").outputSelf().inputNone().chainedInput(true).concrete(false)
									.dataType(Object.class).inputBindingType(ImportSource.class)
									.inputBindingStrategy(InputBindingStrategy.PROVIDED).outputBindingFactoryType(ImportTarget.class)
									.outputBindingType(DataSource.class).outputBindingStrategy(OutputBindingStrategy.PROVIDED_FACTORY)
									.outputBindingMethod("dereferenceImport").outputBindingMethodUnchecked(true)
									.providedOutputBindingMethodParameters("targetModel", "targetId", "this")
									.addChild(c -> c.data().name("targetModel").type(referenceType).dataType(new TypeToken<Model<?>>() {})
											.concrete(false).outputNone().inputNone().valueResolution(
													ValueResolution.REGISTRATION_TIME)
											.addChild(d -> d.data().name("targetModel")
													.provideValue(new BufferingDataTarget()
															.put(Primitive.QUALIFIED_NAME, new QualifiedName("model", namespace)).buffer()))
											.addChild(d -> d.data().name("targetId")
													.provideValue(new BufferingDataTarget()
															.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace)).buffer())))
									.addChild(d -> d.data().type(primitives.get(Primitive.QUALIFIED_NAME)).concrete(false)
											.name("targetId").valueResolution(ValueResolution.REGISTRATION_TIME).outputNone().inputNone())
									.addChild(c -> c.inputSequence().name("importObject").chainedInput(true).uncheckedInput(true)
											.addChild(d -> d.data().dataType(new TypeToken<Model<?>>() {}).name("targetModel")
													.outputNone().inputBindingStrategy(InputBindingStrategy.PROVIDED)
													.inputBindingType(ProcessingContext.class)
													.provideValue(new BufferingDataTarget().buffer())
													.addChild(
															f -> f.data().name("bindingNode")
																	.inputMethod("getBindingNode").type(primitives.get(Primitive.INT)).outputNone()
																	.chainedInput(true)
																	.provideValue(new BufferingDataTarget().put(Primitive.INT, 1).buffer()))
													.addChild(e -> e.data().name("child").type(primitives.get(Primitive.QUALIFIED_NAME))
															.chainedInput(true).outputNone()
															.provideValue(new BufferingDataTarget()
																	.put(Primitive.QUALIFIED_NAME, new QualifiedName("targetModel", namespace)).buffer())
															.postInputType(new TypeToken<DataNode<?>>() {}).castInput(true))
													.addChild(e -> e.inputSequence().name("providedValue").chainedInput(true)))
											.addChild(d -> d.data().dataType(QualifiedName.class).name("targetId").outputNone()
													.inputBindingStrategy(InputBindingStrategy.PROVIDED).inputBindingType(ProcessingContext.class)
													.provideValue(new BufferingDataTarget().buffer())
													.addChild(f -> f.data().name("bindingNode").inputMethod("getBindingNode")
															.type(primitives.get(Primitive.INT)).outputNone().chainedInput(true)
															.provideValue(new BufferingDataTarget().put(Primitive.INT, 1).buffer()))
													.addChild(e -> e.data().name("child").type(primitives.get(Primitive.QUALIFIED_NAME))
															.chainedInput(true).outputNone()
															.provideValue(new BufferingDataTarget()
																	.put(Primitive.QUALIFIED_NAME, new QualifiedName("targetId", namespace)).buffer())
															.postInputType(new TypeToken<DataNode<?>>() {}).castInput(true))
													.addChild(e -> e.inputSequence().name("providedValue").chainedInput(true)))
											.addChild(d -> d.data().name("data").type(bufferedDataType).outputSelf()))));
		}

		@Override
		public DataType<Package> packageType() {
			return packageType;
		}

		@Override
		public DataType<Class<?>> classType() {
			return classType;
		}

		@Override
		public DataType<Type> typeType() {
			return typeType;
		}

		@Override
		public DataType<AnnotatedType> annotatedTypeType() {
			return annotatedTypeType;
		}

		@Override
		public DataType<TypeToken<?>> typeTokenType() {
			return typeTokenType;
		}

		@Override
		public DataType<Enum<?>> enumType() {
			return enumType;
		}

		@Override
		public DataType<Enumeration<?>> enumerationType() {
			return enumerationType;
		}

		@Override
		public DataType<Range<Integer>> rangeType() {
			return rangeType;
		}

		@Override
		public DataType<Object> referenceType() {
			return referenceType;
		}

		@Override
		public DataType<Object> bindingReferenceType() {
			return bindingReferenceType;
		}

		@Override
		public DataType<DataSource> bufferedDataType() {
			return bufferedDataType;
		}

		@Override
		public DataType<DataItem<?>> bufferedDataItemType() {
			return bufferedDataItemType;
		}

		@Override
		public DataType<Object[]> arrayType() {
			return arrayType;
		}

		@Override
		public DataType<Collection<?>> collectionType() {
			return collectionType;
		}

		@Override
		public DataType<List<?>> listType() {
			return listType;
		}

		@Override
		public DataType<Set<?>> setType() {
			return setType;
		}

		@Override
		public DataType<Collection<?>> includeType() {
			return includeType;
		}

		@Override
		public DataType<Object> importType() {
			return importType;
		}

		@Override
		public DataType<URI> uriType() {
			return uriType;
		}

		@Override
		public DataType<URL> urlType() {
			return urlType;
		}
	}

	private class BaseModelsImpl implements BaseModels {
		private Model<?> simpleModel;
		private Model<Map<?, ?>> mapModel;

		public BaseModelsImpl(ModelFactory factory) {
			simpleModel = factory.apply("simpleModel",
					m -> m.dataType(TypeToken.over(AnnotatedWildcardTypes.unbounded(Annotations.from(Infer.class))))
							.concrete(false).inputBindingStrategy(InputBindingStrategy.SOURCE_ADAPTOR)
							.addChild(w -> w.data().name("content").concrete(false).format(Format.CONTENT).outputSelf()));

			/*
			 * Having trouble annotating Map.Entry for some reason, so need this
			 * kludge.
			 */
			AnnotatedType annotatedMapEntry = AnnotatedParameterizedTypes.from(
					AnnotatedTypes.over(Map.Entry.class, Annotations.from(Infer.class)),
					Arrays.asList(AnnotatedWildcardTypes.unbounded(), AnnotatedWildcardTypes.unbounded()));
			TypeToken<?> inferredMapEntry = TypeToken.over(annotatedMapEntry);
			TypeToken<?> inferredMapEntrySet = TypeToken
					.over(AnnotatedParameterizedTypes.from(AnnotatedTypes.over(Set.class, Annotations.from(Infer.class)),
							Arrays.asList(AnnotatedWildcardTypes.upperBounded(annotatedMapEntry))));

			mapModel = factory.apply("map",
					c -> c.dataType(new @Infer TypeToken<Map<?, ?>>() {})
							.addChild(e -> e.complex().name("entrySet").inline(true).inputNone().dataType(inferredMapEntrySet)
									.inputBindingStrategy(InputBindingStrategy.TARGET_ADAPTOR)
									.addChild(s -> s.inputSequence().name("entrySet").chainedInput(true))
									.addChild(f -> f.complex().name("entry").occurrences(Range.between(0, null)).inputMethod("add")
											.outputSelf().inputBindingStrategy(InputBindingStrategy.IMPLEMENT_IN_PLACE)
											.inputBindingType(BaseSchemaImpl.class).outputBindingMethod("mapEntry").dataType(inferredMapEntry)
											.addChild(k -> k.data().name("key").inputNone().format(Format.PROPERTY)
													.dataType(AnnotatedWildcardTypes.unbounded(Annotations.from(Infer.class))).concrete(false)
													.extensible(true))
											.addChild(v -> v.complex().name("value").inputNone()
													.dataType(AnnotatedWildcardTypes.unbounded(Annotations.from(Infer.class))).concrete(false)
													.extensible(true)))));
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

	private final Map<Primitive<?>, DataType<?>> primitives;
	private final DerivedTypes derivedTypes;

	private final BaseModels models;

	public BaseSchemaImpl(SchemaBuilder schemaBuilder, DataLoader loader) {
		QualifiedName name = BaseSchema.QUALIFIED_NAME;
		Namespace namespace = name.getNamespace();

		/*
		 * Schema
		 */
		SchemaConfigurator schemaConfigurator = schemaBuilder.configure(loader).qualifiedName(name);

		/*
		 * Types
		 */
		TypeFactory typeFactory = new TypeFactory() {
			@Override
			public <T> DataType<T> apply(String name,
					Function<DataTypeConfigurator<Object>, DataTypeConfigurator<T>> typeFunction) {
				return typeFunction.apply(schemaConfigurator.addDataType().name(name, namespace)).create();
			}
		};

		DataType<Enumeration<?>> enumerationBaseType = typeFactory.apply("enumerationBase",
				c -> c.outputBindingType(Enumeration.class).inputBindingStrategy(InputBindingStrategy.STATIC_FACTORY)
						.concrete(false).export(false).dataType(new @Infer TypeToken<Enumeration<?>>() {}));

		DataType<Object> primitive = typeFactory.apply("primitive",
				p -> p.concrete(false).export(false).inputBindingType(new TypeToken<DataSource>() {})
						.inputBindingStrategy(InputBindingStrategy.PROVIDED).outputBindingType(new TypeToken<DataTarget>() {})
						.outputBindingStrategy(OutputBindingStrategy.PASS_TO_PROVIDED).outputBindingMethod("put")
						.providedOutputBindingMethodParameters("dataType", "this")
						.addChild(c -> c.data().name("dataType").type(enumerationBaseType).inputMethod("get").concrete(false)
								.extensible(true).chainedInput(true).valueResolution(ValueResolution.REGISTRATION_TIME)
								.dataType(new @Infer TypeToken<Primitive<?>>() {}).outputNone()));

		primitives = new HashMap<>();
		for (Primitive<?> dataType : Enumeration.getConstants(Primitive.class))
			primitives.put(dataType,
					typeFactory.apply(dataType.name(),
							p -> p.baseType(primitive).dataType(dataType.dataClass())
									.addChild(c -> c.data().name("dataType").dataType(resolvePrimitiveDataType(dataType))
											.provideValue(new BufferingDataTarget().put(Primitive.STRING, dataType.name()).buffer()))));

		derivedTypes = new DerivedTypesImpl(typeFactory, enumerationBaseType);

		/*
		 * Models
		 */
		models = new BaseModelsImpl(new ModelFactory() {
			@Override
			public <T> Model<T> apply(String name, Function<ModelConfigurator<Object>, ModelConfigurator<T>> modelFunction) {
				return modelFunction.apply(schemaConfigurator.addModel().name(name, namespace)).create();
			}
		});

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
	public <T> DataType<T> primitiveType(Primitive<T> type) {
		return (DataType<T>) primitives.get(type);
	}

	@Override
	public DerivedTypes derivedTypes() {
		return derivedTypes;
	}

	@Override
	public BaseModels baseModels() {
		return models;
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
	public DataTypes dataTypes() {
		return baseSchema.dataTypes();
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
