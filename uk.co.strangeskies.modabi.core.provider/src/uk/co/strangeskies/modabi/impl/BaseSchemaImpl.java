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

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.DataBindingTypes;
import uk.co.strangeskies.modabi.Models;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.io.DataType;
import uk.co.strangeskies.modabi.processing.BindingContext;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.UnbindingStrategy;
import uk.co.strangeskies.modabi.processing.providers.DereferenceSource;
import uk.co.strangeskies.modabi.processing.providers.ImportReferenceTarget;
import uk.co.strangeskies.modabi.processing.providers.ImportSource;
import uk.co.strangeskies.modabi.processing.providers.IncludeTarget;
import uk.co.strangeskies.modabi.processing.providers.ReferenceTarget;
import uk.co.strangeskies.modabi.schema.DataBindingType;
import uk.co.strangeskies.modabi.schema.DataBindingTypeConfigurator;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataNode.Format;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;
import uk.co.strangeskies.modabi.schema.building.DataBindingTypeBuilder;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.modabi.schema.building.ModelBuilder;
import uk.co.strangeskies.reflection.AnnotatedParameterizedTypes;
import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.AnnotatedWildcardTypes;
import uk.co.strangeskies.reflection.Annotations;
import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypeToken.Infer;
import uk.co.strangeskies.reflection.Types;
import uk.co.strangeskies.utilities.Enumeration;

public class BaseSchemaImpl implements BaseSchema {
	private interface TypeFactory {
		<T> DataBindingType<T> apply(String name,
				Function<DataBindingTypeConfigurator<Object>, DataBindingType<T>> type);
	}

	private interface ModelFactory {
		<T> Model<T> apply(String name,
				Function<ModelConfigurator<Object>, Model<T>> type);
	}

	private class DerivedTypesImpl implements DerivedTypes {
		private final DataBindingType<Object> referenceType;
		private final DataBindingType<DataSource> bufferedDataType;

		private final DataBindingType<Class<?>> classType;
		private final DataBindingType<Type> typeType;
		private final DataBindingType<AnnotatedType> annotatedTypeType;
		private final DataBindingType<TypeToken<?>> typeTokenType;
		private final DataBindingType<Enum<?>> enumType;
		private final DataBindingType<Enumeration<?>> enumerationType;
		private final DataBindingType<Range<Integer>> rangeType;
		private final DataBindingType<Object[]> arrayType;
		private final DataBindingType<Collection<?>> collectionType;
		private final DataBindingType<List<?>> listType;
		private final DataBindingType<Set<?>> setType;
		private final DataBindingType<Object> importType;
		private final DataBindingType<Collection<?>> includeType;

		public DerivedTypesImpl(TypeFactory factory,
				DataBindingType<Enumeration<?>> enumerationBaseType) {
			Namespace namespace = BaseSchema.QUALIFIED_NAME.getNamespace();

			arrayType = factory.apply("array",
					t -> t.dataType(new TypeToken<@Infer Object[]>() {}).isAbstract(true)
							.bindingStrategy(BindingStrategy.PROVIDED).bindingType(List.class)
							.unbindingStrategy(UnbindingStrategy.STATIC_FACTORY)
							.unbindingType(new TypeToken<@Infer List<?>>() {})
							.unbindingFactoryType(Arrays.class).unbindingMethod("asList")
							.addChild(c -> c.data().name("element").inMethod("add")
									.outMethod("this").isAbstract(true)
									.occurrences(Range.create(0, null)).inMethodChained(false)
									.outMethodIterable(true))
					.addChild(c -> c.inputSequence().name("toArray").inMethodChained(true)
							.inMethodCast(true)).create());

			collectionType = factory.apply("collection",
					t -> t.dataType(new TypeToken<@Infer Collection<?>>() {})
							.isAbstract(true).bindingStrategy(BindingStrategy.PROVIDED)
							.unbindingStrategy(UnbindingStrategy.SIMPLE)
							.addChild(c -> c.data().name("element").inMethod("add")
									.outMethod("this").isAbstract(true)
									.occurrences(Range.create(0, null)).outMethodIterable(true))
					.create());

			listType = factory
					.apply("list",
							t -> t.isAbstract(true)
									.dataType(new TypeToken<@Infer List<?>>() {})
									.baseType(collectionType).create());

			setType = factory
					.apply("set",
							t -> t.isAbstract(true)
									.dataType(new TypeToken<@Infer Set<?>>() {})
									.baseType(collectionType).create());

			bufferedDataType = factory.apply("bufferedData",
					t -> t.dataType(DataSource.class).bindingType(DataSource.class)
							.bindingStrategy(BindingStrategy.PROVIDED)
							.unbindingType(DataTarget.class)
							.unbindingStrategy(UnbindingStrategy.ACCEPT_PROVIDED)
							.unbindingMethod("pipe").create());

			DataBindingType<Object> referenceBaseType = factory.apply("referenceBase",
					t -> t
							.<Object> dataType(TypeToken.over(AnnotatedWildcardTypes
									.unbounded(Annotations.from(Infer.class))))
							.isAbstract(true).isPrivate(true)
							.bindingType(DereferenceSource.class)
							.bindingStrategy(BindingStrategy.PROVIDED)
							.unbindingFactoryType(ReferenceTarget.class)
							.unbindingType(DataSource.class).unbindingMethod("reference")
							.unbindingMethodUnchecked(true)
							.unbindingStrategy(UnbindingStrategy.PROVIDED_FACTORY)
							.providedUnbindingMethodParameters("targetModel", "targetId",
									"this")
							.addChild(
									d -> d.data().dataType(new TypeToken<@Infer Model<?>>() {})
											.name("targetModel").isAbstract(true)
											.valueResolution(ValueResolution.REGISTRATION_TIME)
											.inMethod(
													"null")
											.outMethod("null"))
							.addChild(
									d -> d.data().type(primitives.get(DataType.QUALIFIED_NAME))
											.name("targetId").isAbstract(true)
											.valueResolution(ValueResolution.REGISTRATION_TIME)
											.inMethod("null").outMethod("null"))
							.addChild(
									c -> c.inputSequence().name("dereference")
											.inMethodChained(true).inMethodUnchecked(true)
											.addChild(d -> d.data().dataType(
													Model.class)
											/*
											 * TODO make value resolution REGISTRATION_TIME, and make
											 * abstract so that it is delayed until a non-abstract
											 * base is created (thus ensuring targetModel and targetId
											 * provisions will be available)
											 */
											.name("targetModelInput")
											.provideValue(new BufferingDataTarget().buffer())
											.outMethod("null")
											.bindingStrategy(BindingStrategy.PROVIDED)
											.bindingType(BindingContext.class)
											.addChild(
													e -> e.data().name("bindingNode")
															.inMethodChained(true)
															.postInputType(
																	new TypeToken<DataBindingType.Effective<?>>() {})
															.inMethodCast(true).outMethod("null")
															.type(primitives.get(DataType.INT))
															.provideValue(new BufferingDataTarget()
																	.put(DataType.INT, 1).buffer()))
													.addChild(
															e -> e.data().name("child")
																	.type(primitives.get(DataType.QUALIFIED_NAME))
																	.inMethodChained(true)
																	.outMethod(
																			"null")
																	.provideValue(new BufferingDataTarget()
																			.put(DataType.QUALIFIED_NAME,
																					new QualifiedName("targetModel",
																							namespace))
																			.buffer())
													.postInputType(
															new TypeToken<DataNode.Effective<?>>() {})
													.inMethodCast(true))
											.addChild(e -> e.inputSequence().name("providedValue")
													.inMethodChained(true)))
											.addChild(d -> d.data().dataType(QualifiedName.class)
													.name("targetIdInput").outMethod("null")
													.provideValue(new BufferingDataTarget().buffer())
													.bindingStrategy(BindingStrategy.PROVIDED)
													.bindingType(BindingContext.class)
													.addChild(e -> e.data().name("bindingNode")
															.inMethodChained(true)
															.postInputType(
																	new TypeToken<DataBindingType.Effective<?>>() {})
													.type(primitives.get(DataType.INT)).inMethodCast(true)
													.outMethod("null")
													.provideValue(new BufferingDataTarget()
															.put(DataType.INT, 1).buffer()))
													.addChild(e -> e.data().name("child")
															.type(primitives.get(DataType.QUALIFIED_NAME))
															.inMethodChained(true).outMethod("null")
															.provideValue(new BufferingDataTarget()
																	.put(DataType.QUALIFIED_NAME,
																			new QualifiedName("targetId", namespace))
																	.buffer())
													.postInputType(
															new TypeToken<DataNode.Effective<?>>() {})
													.inMethodCast(true))
											.addChild(e -> e.inputSequence().name("providedValue")
													.inMethodChained(true)))
									.addChild(d -> d.data().name("data").type(bufferedDataType)
											.outMethod("this")))
							.create());

			referenceType = factory.apply("reference",
					t -> t.baseType(referenceBaseType).isAbstract(true)
							.addChild(
									c -> c.data().name("targetModel").type(referenceBaseType)
											.isAbstract(true).dataType(Model.class)
											.addChild(d -> d.data().name("targetModel")
													.type(referenceBaseType).extensible(true)
													.dataType(Model.class)
													.provideValue(new BufferingDataTarget()
															.put(DataType.QUALIFIED_NAME,
																	new QualifiedName("model", namespace))
															.buffer())
											.addChild(e -> e.data().name("targetModel")
													.type(referenceBaseType).extensible(true)
													.isAbstract(true).dataType(Model.class)
													.provideValue(new BufferingDataTarget()
															.put(DataType.QUALIFIED_NAME,
																	new QualifiedName("model", namespace))
															.buffer()))
													.addChild(e -> e.data().name("targetId")
															.provideValue(new BufferingDataTarget()
																	.put(DataType.QUALIFIED_NAME,
																			new QualifiedName("name", namespace))
																	.buffer())))
											.addChild(d -> d.data().name("targetId")
													.provideValue(new BufferingDataTarget()
															.put(DataType.QUALIFIED_NAME,
																	new QualifiedName("name", namespace))
															.buffer())))
							.create());

			classType = factory.apply("class",
					t -> t.dataType(new TypeToken<Class<?>>() {})
							.bindingStrategy(BindingStrategy.STATIC_FACTORY)
							.bindingType(Types.class)
							.addChild(p -> p.data().type(primitives.get(DataType.STRING))
									.name("name").inMethod("fromString").inMethodCast(true))
					.create());

			typeType = factory.apply("type",
					t -> t.dataType(Type.class)
							.bindingStrategy(BindingStrategy.STATIC_FACTORY)
							.bindingType(Types.class)
							.addChild(p -> p.data().type(primitives.get(DataType.STRING))
									.name("name").inMethod("fromString").outMethod("toString"))
					.create());

			annotatedTypeType = factory.apply("annotatedType",
					t -> t.dataType(AnnotatedType.class)
							.bindingStrategy(BindingStrategy.STATIC_FACTORY)
							.bindingType(AnnotatedTypes.class)
							.addChild(p -> p.data().type(primitives.get(DataType.STRING))
									.name("name").inMethod("fromString").outMethod("toString"))
					.create());

			typeTokenType = factory.apply("typeToken",
					t -> t.dataType(new TypeToken<TypeToken<?>>() {})
							.bindingStrategy(BindingStrategy.STATIC_FACTORY)
							.addChild(o -> o.data().type(annotatedTypeType)
									.outMethod("getAnnotatedDeclaration").inMethod("over"))
					.create());

			enumType = factory
					.apply("enum",
							t -> t.dataType(new TypeToken<Enum<?>>() {})
									.bindingType(Enumeration.class).isAbstract(true)
									.bindingStrategy(BindingStrategy.STATIC_FACTORY)
									.unbindingStrategy(
											UnbindingStrategy.SIMPLE)
									.addChild(
											n -> n.inputSequence().name("valueOfEnum")
													.addChild(
															o -> o.data()
																	.dataType(
																			new TypeToken<Class<? extends Enum<?>>>() {})
																	.name("enumType").outMethod("null")
																	.provideValue(
																			new BufferingDataTarget().buffer())
											.bindingStrategy(BindingStrategy.PROVIDED)
											.bindingType(BindingContext.class)
											.addChild(e -> e.inputSequence().name("bindingNode")
													.inMethodChained(true)
													.postInputType(
															new TypeToken<DataBindingType.Effective<?>>() {})
											.inMethodCast(true))
									.addChild(p -> p.inputSequence().name("getDataType")
											.inMethodChained(true))
									.addChild(p -> p.inputSequence().name("getRawType")
											.inMethodChained(true)))
							.addChild(o -> o.data().name("name")
									.type(primitives.get(DataType.STRING)))).create());

			enumerationType = factory
					.apply("enumeration",
							t -> t.baseType(enumerationBaseType)
									.isAbstract(
											true)
									.addChild(
											n -> n.inputSequence().name("valueOf").inMethod("valueOf")
													.addChild(
															o -> o.data()
																	.dataType(
																			new TypeToken<Class<? extends Enumeration<?>>>() {})
																	.name("enumerationType").outMethod("null")
																	.provideValue(
																			new BufferingDataTarget().buffer())
													.bindingStrategy(BindingStrategy.PROVIDED)
													.bindingType(BindingContext.class)
													.addChild(e -> e.inputSequence().name("bindingNode")
															.inMethodChained(true)
															.postInputType(
																	new TypeToken<DataBindingType.Effective<?>>() {})
											.inMethodCast(true))
									.addChild(p -> p.inputSequence().name("getDataType")
											.inMethodChained(true))
									.addChild(p -> p.inputSequence().name("getType")
											.inMethodChained(true)))
							.addChild(o -> o.data().name("name")
									.type(primitives.get(DataType.STRING)))).create());

			rangeType = factory.apply("range",
					t -> t.dataType(new TypeToken<Range<Integer>>() {})
							.bindingStrategy(BindingStrategy.STATIC_FACTORY)
							.unbindingStrategy(UnbindingStrategy.STATIC_FACTORY)
							.unbindingType(String.class).unbindingFactoryType(Range.class)
							.addChild(p -> p.data().type(primitives.get(DataType.STRING))
									.outMethod("this").name("string"))
							.create());

			includeType = factory.apply("include",
					t -> t.dataType(new TypeToken<Collection<?>>() {})
							.unbindingType(IncludeTarget.class)
							.bindingStrategy(BindingStrategy.TARGET_ADAPTOR)
							.unbindingStrategy(UnbindingStrategy.PASS_TO_PROVIDED)
							.unbindingMethod("include")
							.providedUnbindingMethodParameters("targetModel", "this")
							.unbindingMethodUnchecked(true).isAbstract(true)
							.addChild(c -> c.data().name("targetModel").type(referenceType)
									.isAbstract(true).dataType(new TypeToken<Model<?>>() {})
									.outMethod("null").inMethod("null")
									.valueResolution(ValueResolution.REGISTRATION_TIME)
									.addChild(d -> d.data().name("targetModel")
											.provideValue(new BufferingDataTarget()
													.put(DataType.QUALIFIED_NAME,
															new QualifiedName("model", namespace))
													.buffer()))
									.addChild(
											d -> d.data().name("targetId")
													.provideValue(new BufferingDataTarget()
															.put(DataType.QUALIFIED_NAME,
																	new QualifiedName("name", namespace))
															.buffer())))
							.addChild(
									c -> c.data().name("in").outMethod("null").inMethod("null")
											.bindingStrategy(
													BindingStrategy.PROVIDED)
											.dataType(
													IncludeTarget.class)
											.addChild(
													d -> d.inputSequence().name("include")
															.inMethod("include")
															.inMethodUnchecked(
																	true)
															.addChild(e -> e.data()
																	.dataType(new TypeToken<Model<?>>() {})
																	.name("targetModel").outMethod("null")
																	.bindingStrategy(BindingStrategy.PROVIDED)
																	.bindingType(
																			BindingContext.class)
													.addChild(
															f -> f.data().name("bindingNode")
																	.type(primitives.get(DataType.INT))
																	.outMethod("null").inMethodChained(true)
																	.provideValue(new BufferingDataTarget()
																			.put(DataType.INT, 2).buffer()))
																	.addChild(
																			f -> f.data().name("child")
																					.type(primitives
																							.get(DataType.QUALIFIED_NAME))
																	.inMethodChained(true)
																	.outMethod(
																			"null").provideValue(
																					new BufferingDataTarget()
																							.put(DataType.QUALIFIED_NAME,
																									new QualifiedName(
																											"targetModel", namespace))
																							.buffer())
															.postInputType(
																	new TypeToken<DataNode.Effective<?>>() {})
															.inMethodCast(true))
													.addChild(f -> f.inputSequence().name("providedValue")
															.inMethodChained(true)))
											.addChild(
													e -> e.data().name("object")
															.dataType(Collection.class).outMethod("null")
															.bindingStrategy(BindingStrategy.PROVIDED)
															.bindingType(BindingContext.class)
															.addChild(f -> f.data().name("bindingTarget")
																	.type(primitives.get(DataType.INT))
																	.inMethodChained(true).outMethod("null")
																	.provideValue(new BufferingDataTarget()
																			.put(DataType.INT, 1).buffer())))))
					.create());

			importType = factory
					.apply("import",
							t -> t.dataType(Object.class)
									.isAbstract(
											true)
									.bindingStrategy(BindingStrategy.SOURCE_ADAPTOR)
									.unbindingStrategy(
											UnbindingStrategy.SIMPLE)
									.unbindingMethod(
											"this")
									.addChild(b -> b.data().name("import").outMethod("this")
											.inMethod("null").inMethodChained(true).isAbstract(true)
											.dataType(Object.class).bindingType(ImportSource.class)
											.bindingStrategy(BindingStrategy.PROVIDED)
											.unbindingFactoryType(ImportReferenceTarget.class)
											.unbindingType(DataSource.class)
											.unbindingStrategy(UnbindingStrategy.PROVIDED_FACTORY)
											.unbindingMethod("dereferenceImport")
											.unbindingMethodUnchecked(true)
											.providedUnbindingMethodParameters("targetModel",
													"targetId", "this")
							.addChild(c -> c.data().name("targetModel").type(referenceType)
									.isAbstract(true).dataType(new TypeToken<Model<?>>() {})
									.outMethod("null").inMethod("null")
									.valueResolution(ValueResolution.REGISTRATION_TIME)
									.addChild(d -> d.data().name("targetModel")
											.provideValue(new BufferingDataTarget()
													.put(DataType.QUALIFIED_NAME,
															new QualifiedName("model", namespace))
													.buffer()))
									.addChild(
											d -> d.data().name("targetId")
													.provideValue(new BufferingDataTarget()
															.put(DataType.QUALIFIED_NAME,
																	new QualifiedName("name", namespace))
															.buffer())))
											.addChild(d -> d.data()
													.type(primitives.get(DataType.QUALIFIED_NAME))
													.isAbstract(true).name("targetId")
													.valueResolution(ValueResolution.REGISTRATION_TIME)
													.outMethod("null").inMethod(
															"null"))
											.addChild(
													c -> c.inputSequence().name("importObject")
															.inMethodChained(true)
															.inMethodUnchecked(
																	true)
															.addChild(d -> d.data()
																	.dataType(new TypeToken<Model<?>>() {})
																	.name("targetModel").outMethod("null")
																	.bindingStrategy(BindingStrategy.PROVIDED)
																	.bindingType(BindingContext.class)
																	.provideValue(
																			new BufferingDataTarget().buffer())
											.addChild(f -> f.data().name("bindingNode")
													.type(primitives.get(DataType.INT)).outMethod("null")
													.inMethodChained(true)
													.provideValue(new BufferingDataTarget()
															.put(DataType.INT, 1).buffer())).addChild(
																	e -> e.data().name("child")
																			.type(primitives
																					.get(DataType.QUALIFIED_NAME))
																			.inMethodChained(true).outMethod("null")
																			.provideValue(
																					new BufferingDataTarget()
																							.put(DataType.QUALIFIED_NAME,
																									new QualifiedName(
																											"targetModel", namespace))
																							.buffer())
																			.postInputType(
																					new TypeToken<DataNode.Effective<?>>() {})
																			.inMethodCast(true))
											.addChild(e -> e.inputSequence().name("providedValue")
													.inMethodChained(true)))
									.addChild(d -> d.data().dataType(QualifiedName.class)
											.name("targetId").outMethod("null")
											.bindingStrategy(BindingStrategy.PROVIDED)
											.bindingType(BindingContext.class)
											.provideValue(new BufferingDataTarget().buffer())
											.addChild(f -> f.data().name("bindingNode")
													.type(primitives.get(DataType.INT)).outMethod("null")
													.inMethodChained(true)
													.provideValue(new BufferingDataTarget()
															.put(DataType.INT, 1).buffer()))
											.addChild(e -> e.data().name("child")
													.type(primitives.get(DataType.QUALIFIED_NAME))
													.inMethodChained(true).outMethod("null")
													.provideValue(new BufferingDataTarget()
															.put(DataType.QUALIFIED_NAME,
																	new QualifiedName("targetId", namespace))
															.buffer())
													.postInputType(
															new TypeToken<DataNode.Effective<?>>() {})
													.inMethodCast(true))
											.addChild(e -> e.inputSequence().name("providedValue")
													.inMethodChained(true)))
									.addChild(d -> d.data().name("data").type(bufferedDataType)
											.outMethod("this"))))
									.create());
		}

		@Override
		public DataBindingType<Class<?>> classType() {
			return classType;
		}

		@Override
		public DataBindingType<Type> typeType() {
			return typeType;
		}

		@Override
		public DataBindingType<AnnotatedType> annotatedTypeType() {
			return annotatedTypeType;
		}

		@Override
		public DataBindingType<TypeToken<?>> typeTokenType() {
			return typeTokenType;
		}

		@Override
		public DataBindingType<Enum<?>> enumType() {
			return enumType;
		}

		@Override
		public DataBindingType<Enumeration<?>> enumerationType() {
			return enumerationType;
		}

		@Override
		public DataBindingType<Range<Integer>> rangeType() {
			return rangeType;
		}

		@Override
		public DataBindingType<Object> referenceType() {
			return referenceType;
		}

		@Override
		public DataBindingType<DataSource> bufferedDataType() {
			return bufferedDataType;
		}

		@Override
		public DataBindingType<Object[]> arrayType() {
			return arrayType;
		}

		@Override
		public DataBindingType<Collection<?>> collectionType() {
			return collectionType;
		}

		@Override
		public DataBindingType<List<?>> listType() {
			return listType;
		}

		@Override
		public DataBindingType<Set<?>> setType() {
			return setType;
		}

		@Override
		public DataBindingType<Collection<?>> includeType() {
			return includeType;
		}

		@Override
		public DataBindingType<Object> importType() {
			return importType;
		}
	}

	private class BaseModelsImpl implements BaseModels {
		private Model<?> simpleModel;
		private Model<Map<?, ?>> mapModel;

		public BaseModelsImpl(ModelFactory factory) {
			simpleModel = factory.apply("simpleModel",
					m -> m
							.dataType(TypeToken.over(AnnotatedWildcardTypes
									.unbounded(Annotations.from(Infer.class))))
							.isAbstract(true).bindingStrategy(BindingStrategy.SOURCE_ADAPTOR)
							.addChild(w -> w.data().name("content").isAbstract(true)
									.format(Format.CONTENT).outMethod("this"))
							.create());

			/*
			 * Having trouble annotating Map.Entry for some reason, so need this
			 * kludge.
			 */
			AnnotatedType annotatedMapEntry = AnnotatedParameterizedTypes.from(
					AnnotatedTypes.over(Map.Entry.class, Annotations.from(Infer.class)),
					Arrays.asList(AnnotatedWildcardTypes.unbounded(),
							AnnotatedWildcardTypes.unbounded()));
			TypeToken<?> inferredMapEntry = TypeToken.over(annotatedMapEntry);
			TypeToken<?> inferredMapEntrySet = TypeToken
					.over(AnnotatedParameterizedTypes.from(
							AnnotatedTypes.over(Set.class, Annotations.from(Infer.class)),
							Arrays.asList(
									AnnotatedWildcardTypes.upperBounded(annotatedMapEntry))));

			mapModel = factory.apply("map", c -> c
					.dataType(
							new TypeToken<@Infer Map<?, ?>>() {})
					.isAbstract(
							true)
					.bindingStrategy(
							BindingStrategy.PROVIDED)
					.addChild(
							e -> e.complex().name("entrySet").inline(true).inMethod("null")
									.isAbstract(
											true)
									.dataType(inferredMapEntrySet)
									.bindingStrategy(BindingStrategy.TARGET_ADAPTOR)
									.unbindingStrategy(UnbindingStrategy.SIMPLE)
									.addChild(s -> s.inputSequence().name("entrySet")
											.inMethodChained(true))
									.addChild(f -> f.complex().name("entry")
											.outMethodIterable(true).inMethod("add").outMethod("this")
											.isAbstract(true)
											.bindingStrategy(BindingStrategy.IMPLEMENT_IN_PLACE)
											.bindingType(BaseSchemaImpl.class)
											.unbindingMethod("mapEntry").dataType(inferredMapEntry)
											.addChild(k -> k.data().name("key").inMethod("null")
													.format(Format.PROPERTY).isAbstract(true))
									.addChild(v -> v.complex().name("value").inMethod("null")
											.isAbstract(true))))
					.create());

			/*-
			 * An example, inferred as type Map<Integer, String>
			 */
			@SuppressWarnings("unchecked")
			Model<Map<?, ?>> m = factory.apply("stringIntMap",
					t -> t.baseModel(mapModel)
							.addChild(u -> u.complex().name("entrySet")
									.addChild(e -> e.complex().name("entry")
											.addChild(k -> k.data().name("key")
													.type(primitiveType(DataType.STRING)))
							.addChild(v -> v.complex()
									.name("value").baseModel(simpleModel).addChild(w -> w.data()
											.name("content").type(primitiveType(DataType.INT))))))
					.create());
			System.out.println(m.effective().getDataType());

			@SuppressWarnings("unchecked")
			Model<Map<List<String>, Double>> m2 = factory.apply("listDoubleMap",
					t -> t.baseModel(mapModel)
							.dataType(new TypeToken<Map<List<String>, Double>>() {})
							.addChild(u -> u.complex().name("entrySet")
									.addChild(e -> e.complex().name("entry")
											.addChild(k -> k.data().name("key"))
											.addChild(v -> v.complex().name("value"))))
							.create());
			System.out.println(m2.effective().getDataType());
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

	private final Map<DataType<?>, DataBindingType<?>> primitives;
	private final DerivedTypes derivedTypes;

	private final BaseModels models;

	public BaseSchemaImpl(SchemaBuilder schemaBuilder, ModelBuilder modelBuilder,
			DataBindingTypeBuilder dataTypeBuilder, DataLoader loader) {
		QualifiedName name = BaseSchema.QUALIFIED_NAME;
		Namespace namespace = name.getNamespace();

		/*
		 * Types
		 */
		Set<DataBindingType<?>> typeSet = new LinkedHashSet<>();

		TypeFactory typeFactory = new TypeFactory() {
			@Override
			public <T> DataBindingType<T> apply(String name,
					Function<DataBindingTypeConfigurator<Object>, DataBindingType<T>> typeFunction) {
				DataBindingType<T> type = typeFunction
						.apply(dataTypeBuilder.configure(loader).name(name, namespace));
				typeSet.add(type);
				return type;
			}
		};

		DataBindingType<Enumeration<?>> enumerationBaseType = typeFactory.apply(
				"enumerationBase",
				c -> c.unbindingType(Enumeration.class)
						.bindingStrategy(BindingStrategy.STATIC_FACTORY).isAbstract(true)
						.isPrivate(true).dataType(new TypeToken<@Infer Enumeration<?>>() {})
						.create());

		DataBindingType<Object> primitive = typeFactory.apply("primitive", p -> p
				.isAbstract(true).isPrivate(true)
				.bindingType(new TypeToken<DataSource>() {})
				.bindingStrategy(BindingStrategy.PROVIDED)
				.unbindingType(new TypeToken<DataTarget>() {})
				.unbindingStrategy(UnbindingStrategy.PASS_TO_PROVIDED)
				.unbindingMethod("put")
				.providedUnbindingMethodParameters("dataType", "this")
				.addChild(c -> c.data().name("dataType").type(enumerationBaseType)
						.inMethod("get").isAbstract(true).extensible(true)
						.inMethodChained(true)
						.valueResolution(ValueResolution.REGISTRATION_TIME)
						.dataType(new TypeToken<@Infer DataType<?>>() {}).outMethod("null"))
				.create());

		primitives = new HashMap<>();
		for (DataType<?> dataType : Enumeration.getConstants(DataType.class))
			primitives.put(dataType,
					typeFactory.apply(dataType.name(),
							p -> p.baseType(primitive).dataType(dataType.dataClass())
									.addChild(c -> c.data().name("dataType")
											.dataType(resolvePrimitiveDataType(dataType))
											.provideValue(new BufferingDataTarget()
													.put(DataType.STRING, dataType.name()).buffer()))
					.create()));

		derivedTypes = new DerivedTypesImpl(typeFactory, enumerationBaseType);

		/*
		 * Models
		 */
		Set<Model<?>> modelSet = new LinkedHashSet<>();

		models = new BaseModelsImpl(new ModelFactory() {
			@Override
			public <T> Model<T> apply(String name,
					Function<ModelConfigurator<Object>, Model<T>> modelFunction) {
				Model<T> model = modelFunction
						.apply(modelBuilder.configure(loader).name(name, namespace));
				modelSet.add(model);
				return model;
			}
		});

		/*
		 * Schema
		 */
		baseSchema = schemaBuilder.configure().qualifiedName(name).types(typeSet)
				.models(modelSet).create();
	}

	private <T> TypeToken<DataType<T>> resolvePrimitiveDataType(
			DataType<T> dataType) {
		return new TypeToken<DataType<T>>() {}.withTypeArgument(
				new TypeParameter<T>() {}, Types.wrapPrimitive(dataType.dataClass()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> DataBindingType<T> primitiveType(DataType<T> type) {
		return (DataBindingType<T>) primitives.get(type);
	}

	@Override
	public DerivedTypes derivedTypes() {
		return derivedTypes;
	}

	@Override
	public BaseModels models() {
		return models;
	}

	/* Schema */

	@Override
	public QualifiedName getQualifiedName() {
		return baseSchema.getQualifiedName();
	}

	@Override
	public Schemata getDependencies() {
		return baseSchema.getDependencies();
	}

	@Override
	public DataBindingTypes getDataTypes() {
		return baseSchema.getDataTypes();
	}

	@Override
	public Models getModels() {
		return baseSchema.getModels();
	}

	@Override
	public boolean equals(Object obj) {
		return baseSchema.equals(obj);
	}

	@Override
	public int hashCode() {
		return baseSchema.hashCode();
	}
}
