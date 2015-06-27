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
package uk.co.strangeskies.modabi.schema.impl;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.HashedMap;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.io.DataType;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.BaseSchema;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.SchemaBuilder;
import uk.co.strangeskies.modabi.schema.Schemata;
import uk.co.strangeskies.modabi.schema.management.DataBindingTypes;
import uk.co.strangeskies.modabi.schema.management.Models;
import uk.co.strangeskies.modabi.schema.management.ValueResolution;
import uk.co.strangeskies.modabi.schema.management.binding.BindingContext;
import uk.co.strangeskies.modabi.schema.management.binding.BindingStrategy;
import uk.co.strangeskies.modabi.schema.management.providers.DereferenceSource;
import uk.co.strangeskies.modabi.schema.management.providers.ImportReferenceTarget;
import uk.co.strangeskies.modabi.schema.management.providers.ImportSource;
import uk.co.strangeskies.modabi.schema.management.providers.IncludeTarget;
import uk.co.strangeskies.modabi.schema.management.providers.ReferenceTarget;
import uk.co.strangeskies.modabi.schema.management.unbinding.UnbindingStrategy;
import uk.co.strangeskies.modabi.schema.node.DataBindingType;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.Model;
import uk.co.strangeskies.modabi.schema.node.building.DataBindingTypeBuilder;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.modabi.schema.node.building.ModelBuilder;
import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypeToken.Infer;
import uk.co.strangeskies.reflection.Types;
import uk.co.strangeskies.utilities.Enumeration;

public class BaseSchemaImpl implements BaseSchema {
	private interface PrimitiveMapping {
		<T> DataBindingType<T> get(DataType<T> dataType);
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

		public DerivedTypesImpl(DataLoader loader, Namespace namespace,
				DataBindingTypeBuilder builder, Set<DataBindingType<?>> typeSet,
				PrimitiveMapping primitives,
				DataBindingType<Enumeration<?>> enumerationBaseType) {
			typeSet.add(arrayType = builder
					.configure(loader)
					.name("array", namespace)
					.dataType(new TypeToken<@Infer Object[]>() {})
					.isAbstract(true)
					.bindingStrategy(BindingStrategy.PROVIDED)
					.bindingType(List.class)
					.unbindingStrategy(UnbindingStrategy.STATIC_FACTORY)
					.unbindingType(new TypeToken<@Infer List<?>>() {})
					.unbindingFactoryType(Arrays.class)
					.unbindingMethod("asList")
					.addChild(
							c -> c.data().name("element").inMethod("add").outMethod("this")
									.isAbstract(true).occurrences(Range.create(0, null))
									.inMethodChained(false).outMethodIterable(true))
					.addChild(
							c -> c.inputSequence().name("toArray").inMethodChained(true)
									.inMethodCast(true)).create());

			typeSet.add(collectionType = builder
					.configure(loader)
					.name("collection", namespace)
					.dataType(new TypeToken<@Infer Collection<?>>() {})
					.isAbstract(true)
					.bindingStrategy(BindingStrategy.PROVIDED)
					.unbindingStrategy(UnbindingStrategy.SIMPLE)
					.addChild(
							c -> c.data().name("element").inMethod("add").outMethod("this")
									.isAbstract(true).occurrences(Range.create(0, null))
									.outMethodIterable(true)).create());

			typeSet.add(listType = builder.configure(loader).name("list", namespace)
					.isAbstract(true).dataType(new TypeToken<@Infer List<?>>() {})
					.baseType(collectionType).create());

			typeSet.add(setType = builder.configure(loader).name("set", namespace)
					.isAbstract(true).dataType(new TypeToken<@Infer Set<?>>() {})
					.baseType(collectionType).create());

			typeSet.add(bufferedDataType = builder.configure(loader)
					.name("bufferedData", namespace).dataType(DataSource.class)
					.bindingType(DataSource.class)
					.bindingStrategy(BindingStrategy.PROVIDED)
					.unbindingType(DataTarget.class)
					.unbindingStrategy(UnbindingStrategy.ACCEPT_PROVIDED)
					.unbindingMethod("pipe").create());

			DataBindingType<Object> referenceBaseType;
			typeSet
					.add(referenceBaseType = builder
							.configure(loader)
							.name("referenceBase", namespace)
							.dataType(Object.class)
							.isAbstract(true)
							.isPrivate(true)
							.bindingType(DereferenceSource.class)
							.bindingStrategy(BindingStrategy.PROVIDED)
							.unbindingFactoryType(ReferenceTarget.class)
							.unbindingType(DataSource.class)
							.unbindingMethod("dereference")
							.unbindingMethodUnchecked(true)
							.unbindingStrategy(UnbindingStrategy.PROVIDED_FACTORY)
							.providedUnbindingMethodParameters("targetModel", "targetId",
									"this")
							.addChild(
									d -> d.data().dataType(Model.class).name("targetModel")
											.bindingType(DereferenceSource.class)
											.bindingStrategy(BindingStrategy.PROVIDED)
											.unbindingFactoryType(ReferenceTarget.class)
											.unbindingType(DataSource.class).isAbstract(true)
											.valueResolution(ValueResolution.REGISTRATION_TIME)
											.inMethod("null").outMethod("null"))
							.addChild(
									d -> d.data().type(primitives.get(DataType.QUALIFIED_NAME))
											.isAbstract(true).name("targetId")
											.valueResolution(ValueResolution.REGISTRATION_TIME)
											.inMethod("null").outMethod("null"))
							.addChild(
									c -> c
											.inputSequence()
											.name("reference")
											.inMethodChained(true)
											.inMethodUnchecked(true)
											.addChild(
													d -> d
															.data()
															.dataType(Model.class)
															.name("targetModel")
															.provideValue(new BufferingDataTarget().buffer())
															.outMethod("null")
															.bindingStrategy(BindingStrategy.PROVIDED)
															.bindingType(BindingContext.class)
															.addChild(
																	e -> e
																			.data()
																			.name("bindingNode")
																			.inMethodChained(true)
																			.postInputType(
																					new TypeToken<DataBindingType.Effective<?>>() {})
																			.inMethodCast(true)
																			.outMethod("null")
																			.type(primitives.get(DataType.INT))
																			.provideValue(
																					new BufferingDataTarget().put(
																							DataType.INT, 1).buffer()))
															.addChild(
																	e -> e
																			.data()
																			.name("child")
																			.type(
																					primitives
																							.get(DataType.QUALIFIED_NAME))
																			.inMethodChained(true)
																			.outMethod("null")
																			.provideValue(
																					new BufferingDataTarget().put(
																							DataType.QUALIFIED_NAME,
																							new QualifiedName("targetModel",
																									namespace)).buffer())
																			.postInputType(
																					new TypeToken<DataNode.Effective<?>>() {})
																			.inMethodCast(true))
															.addChild(
																	e -> e.inputSequence().name("providedValue")
																			.inMethodChained(true)))
											.addChild(
													d -> d
															.data()
															.dataType(QualifiedName.class)
															.name("targetId")
															.outMethod("null")
															.provideValue(new BufferingDataTarget().buffer())
															.bindingStrategy(BindingStrategy.PROVIDED)
															.bindingType(BindingContext.class)
															.addChild(
																	e -> e
																			.data()
																			.name("bindingNode")
																			.inMethodChained(true)
																			.postInputType(
																					new TypeToken<DataBindingType.Effective<?>>() {})
																			.type(primitives.get(DataType.INT))
																			.inMethodCast(true)
																			.outMethod("null")
																			.provideValue(
																					new BufferingDataTarget().put(
																							DataType.INT, 1).buffer()))
															.addChild(
																	e -> e
																			.data()
																			.name("child")
																			.type(
																					primitives
																							.get(DataType.QUALIFIED_NAME))
																			.inMethodChained(true)
																			.outMethod("null")
																			.provideValue(
																					new BufferingDataTarget().put(
																							DataType.QUALIFIED_NAME,
																							new QualifiedName("targetId",
																									namespace)).buffer())
																			.postInputType(
																					new TypeToken<DataNode.Effective<?>>() {})
																			.inMethodCast(true))
															.addChild(
																	e -> e.inputSequence().name("providedValue")
																			.inMethodChained(true)))
											.addChild(
													d -> d.data().name("data").type(bufferedDataType)
															.outMethod("this"))).create());
			typeSet.add(referenceBaseType);

			typeSet.add(referenceType = builder
					.configure(loader)
					.name("reference", namespace)
					.baseType(referenceBaseType)
					.isAbstract(true)
					.addChild(
							c -> c
									.data()
									.name("targetModel")
									.type(referenceBaseType)
									.isAbstract(true)
									.dataType(Model.class)
									.addChild(
											d -> d
													.data()
													.name("targetModel")
													.type(referenceBaseType)
													.extensible(true)
													.dataType(Model.class)
													.provideValue(
															new BufferingDataTarget().put(
																	DataType.QUALIFIED_NAME,
																	new QualifiedName("model", namespace))
																	.buffer())
													.addChild(
															e -> e
																	.data()
																	.name("targetModel")
																	.type(referenceBaseType)
																	.extensible(true)
																	.isAbstract(true)
																	.dataType(Model.class)
																	.provideValue(
																			new BufferingDataTarget()
																					.put(
																							DataType.QUALIFIED_NAME,
																							new QualifiedName("model",
																									namespace)).buffer()))
													.addChild(
															e -> e
																	.data()
																	.name("targetId")
																	.provideValue(
																			new BufferingDataTarget().put(
																					DataType.QUALIFIED_NAME,
																					new QualifiedName("name", namespace))
																					.buffer())))
									.addChild(
											d -> d
													.data()
													.name("targetId")
													.provideValue(
															new BufferingDataTarget().put(
																	DataType.QUALIFIED_NAME,
																	new QualifiedName("name", namespace))
																	.buffer()))).create());

			typeSet.add(classType = builder
					.configure(loader)
					.name("class", namespace)
					.dataType(new TypeToken<Class<?>>() {})
					.bindingStrategy(BindingStrategy.STATIC_FACTORY)
					.bindingType(Types.class)
					.addChild(
							p -> p.data().type(primitives.get(DataType.STRING)).name("name")
									.inMethod("fromString").inMethodCast(true)).create());

			typeSet.add(typeType = builder
					.configure(loader)
					.name("type", namespace)
					.dataType(Type.class)
					.bindingStrategy(BindingStrategy.STATIC_FACTORY)
					.bindingType(Types.class)
					.addChild(
							p -> p.data().type(primitives.get(DataType.STRING)).name("name")
									.inMethod("fromString").outMethod("toString")).create());

			typeSet.add(annotatedTypeType = builder
					.configure(loader)
					.name("annotatedType", namespace)
					.dataType(AnnotatedType.class)
					.bindingStrategy(BindingStrategy.STATIC_FACTORY)
					.bindingType(AnnotatedTypes.class)
					.addChild(
							p -> p.data().type(primitives.get(DataType.STRING)).name("name")
									.inMethod("fromString").outMethod("toString")).create());

			typeSet.add(typeTokenType = builder
					.configure(loader)
					.name("typeTokenType", namespace)
					.dataType(new TypeToken<TypeToken<?>>() {})
					.bindingStrategy(BindingStrategy.STATIC_FACTORY)
					.addChild(
							o -> o.data().type(annotatedTypeType)
									.outMethod("getAnnotatedDeclaration").inMethod("over"))
					.create());

			typeSet
					.add(enumType = builder
							.configure(loader)
							.name("enum", namespace)
							.dataType(new TypeToken<Enum<?>>() {})
							.bindingType(Enumeration.class)
							.isAbstract(true)
							.bindingStrategy(BindingStrategy.STATIC_FACTORY)
							.unbindingStrategy(UnbindingStrategy.SIMPLE)
							.addChild(
									n -> n
											.inputSequence()
											.name("valueOfEnum")
											.addChild(
													o -> o
															.data()
															.dataType(
																	new TypeToken<Class<? extends Enum<?>>>() {})
															.name("enumType")
															.outMethod("null")
															.provideValue(new BufferingDataTarget().buffer())
															.bindingStrategy(BindingStrategy.PROVIDED)
															.bindingType(BindingContext.class)
															.addChild(
																	e -> e
																			.inputSequence()
																			.name("bindingNode")
																			.inMethodChained(true)
																			.postInputType(
																					new TypeToken<DataBindingType.Effective<?>>() {})
																			.inMethodCast(true))
															.addChild(
																	p -> p.inputSequence().name("getDataType")
																			.inMethodChained(true))
															.addChild(
																	p -> p.inputSequence().name("getRawType")
																			.inMethodChained(true)))
											.addChild(
													o -> o.data().name("name")
															.type(primitives.get(DataType.STRING)))).create());

			typeSet
					.add(enumerationType = builder
							.configure(loader)
							.name("enumeration", namespace)
							.baseType(enumerationBaseType)
							.isAbstract(true)
							.addChild(
									n -> n
											.inputSequence()
											.name("valueOf")
											.inMethod("valueOf")
											.addChild(
													o -> o
															.data()
															.dataType(
																	new TypeToken<Class<? extends Enumeration<?>>>() {})
															.name("enumerationType")
															.outMethod("null")
															.provideValue(new BufferingDataTarget().buffer())
															.bindingStrategy(BindingStrategy.PROVIDED)
															.bindingType(BindingContext.class)
															.addChild(
																	e -> e
																			.inputSequence()
																			.name("bindingNode")
																			.inMethodChained(true)
																			.postInputType(
																					new TypeToken<DataBindingType.Effective<?>>() {})
																			.inMethodCast(true))
															.addChild(
																	p -> p.inputSequence().name("getDataType")
																			.inMethodChained(true))
															.addChild(
																	p -> p.inputSequence().name("getType")
																			.inMethodChained(true)))
											.addChild(
													o -> o.data().name("name")
															.type(primitives.get(DataType.STRING)))).create());

			typeSet.add(rangeType = builder
					.configure(loader)
					.name("range", namespace)
					.dataType(new TypeToken<Range<Integer>>() {})
					.bindingStrategy(BindingStrategy.STATIC_FACTORY)
					.unbindingStrategy(UnbindingStrategy.STATIC_FACTORY)
					.unbindingType(String.class)
					.unbindingFactoryType(Range.class)
					.addChild(
							p -> p.data().type(primitives.get(DataType.STRING))
									.outMethod("this").name("string")).create());

			typeSet
					.add(includeType = builder
							.configure(loader)
							.name("include", namespace)
							.dataType(new TypeToken<Collection<?>>() {})
							.unbindingType(IncludeTarget.class)
							.bindingStrategy(BindingStrategy.TARGET_ADAPTOR)
							.unbindingStrategy(UnbindingStrategy.PASS_TO_PROVIDED)
							.unbindingMethod("include")
							.providedUnbindingMethodParameters("targetModel", "this")
							.unbindingMethodUnchecked(true)
							.isAbstract(true)
							.addChild(
									c -> c
											.data()
											.name("targetModel")
											.type(referenceType)
											.isAbstract(true)
											.dataType(new TypeToken<Model<?>>() {})
											.outMethod("null")
											.inMethod("null")
											.valueResolution(ValueResolution.REGISTRATION_TIME)
											.addChild(
													d -> d
															.data()
															.name("targetModel")
															.provideValue(
																	new BufferingDataTarget().put(
																			DataType.QUALIFIED_NAME,
																			new QualifiedName("model", namespace))
																			.buffer()))
											.addChild(
													d -> d
															.data()
															.name("targetId")
															.provideValue(
																	new BufferingDataTarget().put(
																			DataType.QUALIFIED_NAME,
																			new QualifiedName("name", namespace))
																			.buffer())))
							.addChild(
									c -> c
											.data()
											.name("in")
											.outMethod("null")
											.inMethod("null")
											.bindingStrategy(BindingStrategy.PROVIDED)
											.dataType(IncludeTarget.class)
											.addChild(
													d -> d
															.inputSequence()
															.name("include")
															.inMethod("include")
															.inMethodUnchecked(true)
															.addChild(
																	e -> e
																			.data()
																			.dataType(new TypeToken<Model<?>>() {})
																			.name("targetModel")
																			.outMethod("null")
																			.bindingStrategy(BindingStrategy.PROVIDED)
																			.bindingType(BindingContext.class)
																			.addChild(
																					f -> f
																							.data()
																							.name("bindingNode")
																							.type(
																									primitives.get(DataType.INT))
																							.outMethod("null")
																							.inMethodChained(true)
																							.provideValue(
																									new BufferingDataTarget()
																											.put(DataType.INT, 2)
																											.buffer()))
																			.addChild(
																					f -> f
																							.data()
																							.name("child")
																							.type(
																									primitives
																											.get(DataType.QUALIFIED_NAME))
																							.inMethodChained(true)
																							.outMethod("null")
																							.provideValue(
																									new BufferingDataTarget()
																											.put(
																													DataType.QUALIFIED_NAME,
																													new QualifiedName(
																															"targetModel",
																															namespace))
																											.buffer())
																							.postInputType(
																									new TypeToken<DataNode.Effective<?>>() {})
																							.inMethodCast(true))
																			.addChild(
																					f -> f.inputSequence()
																							.name("providedValue")
																							.inMethodChained(true)))
															.addChild(
																	e -> e
																			.data()
																			.name("object")
																			.dataType(Collection.class)
																			.outMethod("null")
																			.bindingStrategy(BindingStrategy.PROVIDED)
																			.bindingType(BindingContext.class)
																			.addChild(
																					f -> f
																							.data()
																							.name("bindingTarget")
																							.type(
																									primitives.get(DataType.INT))
																							.inMethodChained(true)
																							.outMethod("null")
																							.provideValue(
																									new BufferingDataTarget()
																											.put(DataType.INT, 1)
																											.buffer()))))).create());

			typeSet
					.add(importType = builder
							.configure(loader)
							.name("import", namespace)
							.dataType(Object.class)
							.isAbstract(true)
							.bindingStrategy(BindingStrategy.SOURCE_ADAPTOR)
							.unbindingStrategy(UnbindingStrategy.SIMPLE)
							.unbindingMethod("this")
							.addChild(
									b -> b
											.data()
											.name("import")
											.outMethod("this")
											.inMethod("null")
											.inMethodChained(true)
											.isAbstract(true)
											.dataType(Object.class)
											.bindingType(ImportSource.class)
											.bindingStrategy(BindingStrategy.PROVIDED)
											.unbindingFactoryType(ImportReferenceTarget.class)
											.unbindingType(DataSource.class)
											.unbindingStrategy(UnbindingStrategy.PROVIDED_FACTORY)
											.unbindingMethod("dereferenceImport")
											.unbindingMethodUnchecked(true)
											.providedUnbindingMethodParameters("targetModel",
													"targetId", "this")
											.addChild(
													c -> c
															.data()
															.name("targetModel")
															.type(referenceType)
															.isAbstract(true)
															.dataType(new TypeToken<Model<?>>() {})
															.outMethod("null")
															.inMethod("null")
															.valueResolution(
																	ValueResolution.REGISTRATION_TIME)
															.addChild(
																	d -> d
																			.data()
																			.name("targetModel")
																			.provideValue(
																					new BufferingDataTarget().put(
																							DataType.QUALIFIED_NAME,
																							new QualifiedName("model",
																									namespace)).buffer()))
															.addChild(
																	d -> d
																			.data()
																			.name("targetId")
																			.provideValue(
																					new BufferingDataTarget().put(
																							DataType.QUALIFIED_NAME,
																							new QualifiedName("name",
																									namespace)).buffer())))
											.addChild(
													d -> d
															.data()
															.type(primitives.get(DataType.QUALIFIED_NAME))
															.isAbstract(true)
															.name("targetId")
															.valueResolution(
																	ValueResolution.REGISTRATION_TIME)
															.outMethod("null").inMethod("null"))
											.addChild(
													c -> c
															.inputSequence()
															.name("importObject")
															.inMethodChained(true)
															.inMethodUnchecked(true)
															.addChild(
																	d -> d
																			.data()
																			.dataType(new TypeToken<Model<?>>() {})
																			.name("targetModel")
																			.outMethod("null")
																			.bindingStrategy(BindingStrategy.PROVIDED)
																			.bindingType(BindingContext.class)
																			.provideValue(
																					new BufferingDataTarget().buffer())
																			.addChild(
																					f -> f
																							.data()
																							.name("bindingNode")
																							.type(
																									primitives.get(DataType.INT))
																							.outMethod("null")
																							.inMethodChained(true)
																							.provideValue(
																									new BufferingDataTarget()
																											.put(DataType.INT, 1)
																											.buffer()))
																			.addChild(
																					e -> e
																							.data()
																							.name("child")
																							.type(
																									primitives
																											.get(DataType.QUALIFIED_NAME))
																							.inMethodChained(true)
																							.outMethod("null")
																							.provideValue(
																									new BufferingDataTarget()
																											.put(
																													DataType.QUALIFIED_NAME,
																													new QualifiedName(
																															"targetModel",
																															namespace))
																											.buffer())
																							.postInputType(
																									new TypeToken<DataNode.Effective<?>>() {})
																							.inMethodCast(true))
																			.addChild(
																					e -> e.inputSequence()
																							.name("providedValue")
																							.inMethodChained(true)))
															.addChild(
																	d -> d
																			.data()
																			.dataType(QualifiedName.class)
																			.name("targetId")
																			.outMethod("null")
																			.bindingStrategy(BindingStrategy.PROVIDED)
																			.bindingType(BindingContext.class)
																			.provideValue(
																					new BufferingDataTarget().buffer())
																			.addChild(
																					f -> f
																							.data()
																							.name("bindingNode")
																							.type(
																									primitives.get(DataType.INT))
																							.outMethod("null")
																							.inMethodChained(true)
																							.provideValue(
																									new BufferingDataTarget()
																											.put(DataType.INT, 1)
																											.buffer()))
																			.addChild(
																					e -> e
																							.data()
																							.name("child")
																							.type(
																									primitives
																											.get(DataType.QUALIFIED_NAME))
																							.inMethodChained(true)
																							.outMethod("null")
																							.provideValue(
																									new BufferingDataTarget()
																											.put(
																													DataType.QUALIFIED_NAME,
																													new QualifiedName(
																															"targetId",
																															namespace))
																											.buffer())
																							.postInputType(
																									new TypeToken<DataNode.Effective<?>>() {})
																							.inMethodCast(true))
																			.addChild(
																					e -> e.inputSequence()
																							.name("providedValue")
																							.inMethodChained(true)))
															.addChild(
																	d -> d.data().name("data")
																			.type(bufferedDataType).outMethod("this"))))
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
		public BaseModelsImpl(DataLoader loader, Namespace namespace,
				ModelBuilder builder, Set<Model<?>> modelSet) {}
	}

	private final Schema baseSchema;

	private final Map<DataType<?>, DataBindingType<?>> primitives;
	private final DerivedTypes derivedTypes;

	private final BaseModels models;

	public BaseSchemaImpl(SchemaBuilder schemaBuilder, ModelBuilder modelBuilder,
			DataBindingTypeBuilder dataTypeBuilder, DataLoader loader) {
		Namespace namespace = BaseSchema.NAMESPACE;
		QualifiedName name = BaseSchema.QUALIFIED_NAME;

		/*
		 * Types
		 */
		Set<DataBindingType<?>> typeSet = new LinkedHashSet<>();

		DataBindingType<Enumeration<?>> enumerationBaseType;
		typeSet.add(enumerationBaseType = dataTypeBuilder.configure(loader)
				.unbindingType(Enumeration.class)
				.bindingStrategy(BindingStrategy.STATIC_FACTORY)
				.name("enumerationBase", namespace).isAbstract(true).isPrivate(true)
				.dataType(new TypeToken<@Infer Enumeration<?>>() {}).create());

		DataBindingType<Object> primitive;
		typeSet.add(primitive = dataTypeBuilder
				.configure(loader)
				.name("primitive", namespace)
				.isAbstract(true)
				.isPrivate(true)
				.bindingType(new TypeToken<DataSource>() {})
				.bindingStrategy(BindingStrategy.PROVIDED)
				.unbindingType(new TypeToken<DataTarget>() {})
				.unbindingStrategy(UnbindingStrategy.PASS_TO_PROVIDED)
				.unbindingMethod("put")
				.providedUnbindingMethodParameters("dataType", "this")
				.addChild(
						c -> c.data().name("dataType").type(enumerationBaseType)
								.inMethod("get").isAbstract(true).extensible(true)
								.inMethodChained(true)
								.valueResolution(ValueResolution.REGISTRATION_TIME)
								.dataType(new TypeToken<@Infer DataType<?>>() {})
								.outMethod("null")).create());

		primitives = new HashedMap<>();
		for (DataType<?> dataType : Enumeration.getConstants(DataType.class))
			primitives.put(
					dataType,
					dataTypeBuilder
							.configure(loader)
							.name(dataType.name(), namespace)
							.baseType(primitive)
							.dataType(dataType.dataClass())
							.addChild(
									c -> c
											.data()
											.name("dataType")
											.dataType(resolvePrimitiveDataType(dataType))
											.provideValue(
													new BufferingDataTarget().put(DataType.STRING,
															dataType.name()).buffer())).create());

		primitives.values().stream().forEach(typeSet::add);

		derivedTypes = new DerivedTypesImpl(loader, namespace, dataTypeBuilder,
				typeSet, new PrimitiveMapping() {
					@SuppressWarnings("unchecked")
					@Override
					public <T> DataBindingType<T> get(DataType<T> dataType) {
						return (DataBindingType<T>) primitives.get(dataType);
					}
				}, enumerationBaseType);

		/*
		 * Models
		 */
		Set<Model<?>> modelSet = new LinkedHashSet<>();

		models = new BaseModelsImpl(loader, namespace, modelBuilder, modelSet);

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
}
