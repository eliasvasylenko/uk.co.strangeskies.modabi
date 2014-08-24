package uk.co.strangeskies.modabi.schema.processing.impl;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.HashedMap;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.gears.utilities.Enumeration;
import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.DataBindingTypeBuilder;
import uk.co.strangeskies.modabi.data.DataBindingTypes;
import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.data.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.data.io.DataTarget;
import uk.co.strangeskies.modabi.data.io.DataType;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.Models;
import uk.co.strangeskies.modabi.model.building.DataLoader;
import uk.co.strangeskies.modabi.model.building.ModelBuilder;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.BaseSchema;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.SchemaBuilder;
import uk.co.strangeskies.modabi.schema.Schemata;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.RegistrationTimeTargetAdapter;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.ValueResolution;
import uk.co.strangeskies.modabi.schema.processing.reference.DereferenceTarget;
import uk.co.strangeskies.modabi.schema.processing.reference.ReferenceSource;

public class BaseSchemaImpl implements BaseSchema {
	private class DerivedTypesImpl implements DerivedTypes {
		private final DataBindingType<Object> referenceType;
		private final DataBindingType<BufferedDataSource> bufferedDataType;

		@SuppressWarnings("rawtypes")
		private final DataBindingType<Class> classType;
		@SuppressWarnings("rawtypes")
		private final DataBindingType<Enum> enumType;
		@SuppressWarnings("rawtypes")
		private final DataBindingType<Enumeration> enumerationType;
		@SuppressWarnings("rawtypes")
		private final DataBindingType<Range> rangeType;
		@SuppressWarnings("rawtypes")
		private final DataBindingType<Collection> collectionType;
		@SuppressWarnings("rawtypes")
		private final DataBindingType<List> listType;
		@SuppressWarnings("rawtypes")
		private final DataBindingType<Set> setType;

		public DerivedTypesImpl(
				DataLoader loader,
				Namespace namespace,
				DataBindingTypeBuilder builder,
				Set<DataBindingType<?>> typeSet,
				Map<DataType<?>, DataBindingType<?>> primitives,
				@SuppressWarnings("rawtypes") DataBindingType<Enumeration> enumerationBaseType) {
			typeSet.add(collectionType = builder
					.configure(loader)
					.name("collection", namespace)
					.dataClass(Collection.class)
					.isAbstract(true)
					.bindingStrategy(BindingStrategy.PROVIDED)
					.addChild(
							c -> c.data().name("element").inMethod("add").outMethod("this")
									.occurances(Range.create(0, null)).outMethodIterable(true))
					.create());

			typeSet.add(listType = builder.configure(loader).name("list", namespace)
					.dataClass(List.class).baseType(collectionType).create());

			typeSet.add(setType = builder.configure(loader).name("set", namespace)
					.dataClass(Set.class).baseType(collectionType).create());

			typeSet.add(bufferedDataType = builder.configure(loader)
					.name("bufferedData", namespace).dataClass(BufferedDataSource.class)
					.bindingClass(BufferedDataSource.class)
					.bindingStrategy(BindingStrategy.PROVIDED)
					.unbindingClass(DataTarget.class)
					.unbindingStrategy(UnbindingStrategy.ACCEPT_PROVIDED)
					.unbindingMethod("pipe").create());

			DataBindingType<Object> referenceBaseType;
			typeSet
					.add(referenceBaseType = builder
							.configure(loader)
							.name("referenceBase", namespace)
							.dataClass(Object.class)
							.isAbstract(true)
							.isPrivate(true)
							.bindingClass(ReferenceSource.class)
							.bindingStrategy(BindingStrategy.PROVIDED)
							.unbindingFactoryClass(DereferenceTarget.class)
							.unbindingClass(BufferedDataSource.class)
							.unbindingMethod("dereference")
							.unbindingStrategy(UnbindingStrategy.PROVIDED_FACTORY)
							.providedUnbindingParameters("targetModel", "targetId", "this")
							.addChild(
									d -> d.data().dataClass(Model.class).name("targetModel")
											.isAbstract(true)
											.valueResolution(ValueResolution.REGISTRATION_TIME)
											.inMethod("null").outMethod("null"))
							.addChild(
									d -> d.data().type(primitives.get(DataType.STRING))
											.isAbstract(true).name("targetId")
											.valueResolution(ValueResolution.REGISTRATION_TIME)
											.inMethod("null").outMethod("null"))
							.addChild(
									c -> c
											.inputSequence()
											.name("reference")
											.addChild(
													d -> d
															.data()
															.dataClass(Model.class)
															.name("targetModel")
															.isAbstract(true)
															.outMethod("null")
															.valueResolution(
																	ValueResolution.REGISTRATION_TIME)
															.bindingStrategy(BindingStrategy.TARGET_ADAPTOR)
															.bindingClass(RegistrationTimeTargetAdapter.class)
															.addChild(
																	e -> e
																			.data()
																			.name("parent")
																			.type(primitives.get(DataType.INT))
																			.inMethodChained(true)
																			.outMethod("null")
																			.provideValue(
																					new BufferingDataTarget().put(
																							DataType.INT, 1).buffer()))
															.addChild(
																	e -> e
																			.data()
																			.name("node")
																			.type(primitives.get(DataType.STRING))
																			.inMethodChained(true)
																			.outMethod("null")
																			.provideValue(
																					new BufferingDataTarget().put(
																							DataType.STRING, "targetModel")
																							.buffer()))
															.addChild(
																	e -> e.inputSequence().name("providedValue")
																			.inMethodChained(true)))
											.addChild(
													d -> d
															.data()
															.dataClass(String.class)
															.name("targetId")
															.outMethod("null")
															.isAbstract(true)
															.valueResolution(
																	ValueResolution.REGISTRATION_TIME)
															.bindingStrategy(BindingStrategy.TARGET_ADAPTOR)
															.bindingClass(RegistrationTimeTargetAdapter.class)
															.addChild(
																	e -> e
																			.data()
																			.name("parent")
																			.type(primitives.get(DataType.INT))
																			.inMethodChained(true)
																			.outMethod("null")
																			.provideValue(
																					new BufferingDataTarget().put(
																							DataType.INT, 1).buffer()))
															.addChild(
																	e -> e
																			.data()
																			.name("node")
																			.type(primitives.get(DataType.STRING))
																			.inMethodChained(true)
																			.outMethod("null")
																			.provideValue(
																					new BufferingDataTarget().put(
																							DataType.STRING, "targetId")
																							.buffer()))
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
									.isExtensible(true)
									.isAbstract(true)
									.dataClass(Model.class)
									.addChild(
											d -> d
													.data()
													.name("targetModel")
													.provideValue(
															new BufferingDataTarget().put(DataType.STRING,
																	"schema.modabi.strangeskies.co.uk:Model")
																	.buffer()))
									.addChild(
											d -> d
													.data()
													.name("targetId")
													.provideValue(
															new BufferingDataTarget().put(DataType.STRING,
																	"name").buffer()))).create());

			typeSet.add(classType = builder.configure(loader)
					.name("class", namespace).dataClass(Class.class)
					.bindingStrategy(BindingStrategy.STATIC_FACTORY).addChild(
					// TODO outMethod not being carried through to unbinding process...
							p -> p.data().type(primitives.get(DataType.STRING)).name("name"))
					.create());

			typeSet.add(enumType = builder
					.configure(loader)
					.name("enum", namespace)
					.dataClass(Enum.class)
					.isAbstract(true)
					.addChild(
							n -> n
									.inputSequence()
									.name("valueOf")
									.addChild(
											o -> o
													.data()
													.dataClass(Model.class)
													.name("enumType")
													.outMethod("null")
													.provideValue(new BufferingDataTarget().buffer())
													.valueResolution(ValueResolution.REGISTRATION_TIME)
													.bindingStrategy(BindingStrategy.TARGET_ADAPTOR)
													.bindingClass(RegistrationTimeTargetAdapter.class)
													.addChild(
															p -> p
																	.data()
																	.name("parent")
																	.type(primitives.get(DataType.INT))
																	.inMethodChained(true)
																	.outMethod("null")
																	.provideValue(
																			new BufferingDataTarget().put(
																					DataType.INT, 2).buffer()))
													.addChild(
															p -> p.inputSequence().name("getDataClass")
																	.inMethodChained(true)))
									.addChild(
											o -> o.data().name("name")
													.type(primitives.get(DataType.STRING)))).create());

			typeSet.add(enumerationType = builder
					.configure(loader)
					.name("enumeration", namespace)
					.baseType(enumerationBaseType)
					.isAbstract(true)
					.addChild(
							n -> n
									.inputSequence()
									.name("valueOf")
									.addChild(
											o -> o
													.data()
													.dataClass(Model.class)
													.name("enumerationType")
													.outMethod("null")
													.provideValue(new BufferingDataTarget().buffer())
													.valueResolution(ValueResolution.REGISTRATION_TIME)
													.bindingStrategy(BindingStrategy.TARGET_ADAPTOR)
													.bindingClass(RegistrationTimeTargetAdapter.class)
													.addChild(
															p -> p
																	.data()
																	.name("parent")
																	.type(primitives.get(DataType.INT))
																	.inMethodChained(true)
																	.outMethod("null")
																	.provideValue(
																			new BufferingDataTarget().put(
																					DataType.INT, 2).buffer()))
													.addChild(
															p -> p.inputSequence().name("getDataClass")
																	.inMethodChained(true)))
									.addChild(
											o -> o.data().name("name")
													.type(primitives.get(DataType.STRING)))).create());

			typeSet.add(rangeType = builder
					.configure(loader)
					.name("range", namespace)
					.dataClass(Range.class)
					.bindingStrategy(BindingStrategy.STATIC_FACTORY)
					.unbindingStrategy(UnbindingStrategy.STATIC_FACTORY)
					.unbindingClass(String.class)
					.unbindingFactoryClass(Range.class)
					.addChild(
							p -> p.data().type(primitives.get(DataType.STRING))
									.name("string")).create());
		}

		@Override
		@SuppressWarnings("rawtypes")
		public DataBindingType<Class> classType() {
			return classType;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public DataBindingType<Enum> enumType() {
			return enumType;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public DataBindingType<Enumeration> enumerationType() {
			return enumerationType;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public DataBindingType<Range> rangeType() {
			return rangeType;
		}

		@Override
		public DataBindingType<Object> referenceType() {
			return referenceType;
		}

		@Override
		public DataBindingType<BufferedDataSource> bufferedDataType() {
			return bufferedDataType;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public DataBindingType<Collection> collectionType() {
			return collectionType;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public DataBindingType<List> listType() {
			return listType;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public DataBindingType<Set> setType() {
			return setType;
		}
	}

	private class BaseModelsImpl implements BaseModels {
		private final Model<Object> includeModel;

		public BaseModelsImpl(DataLoader loader, Namespace namespace,
				ModelBuilder builder, Set<Model<?>> modelSet) {
			includeModel = builder.configure(loader).name("include", namespace)
					.bindingClass(Object.class).dataClass(Object.class).create();
			modelSet.add(includeModel);
		}

		@Override
		public Model<Object> includeModel() {
			return includeModel;
		}
	}

	private final Schema baseSchema;

	private final Map<DataType<?>, DataBindingType<?>> primitives;
	private final DerivedTypes derivedTypes;

	private final BaseModels models;

	@SuppressWarnings({ "rawtypes" })
	public BaseSchemaImpl(SchemaBuilder schemaBuilder, ModelBuilder modelBuilder,
			DataBindingTypeBuilder dataTypeBuilder, DataLoader loader) {
		Namespace namespace = new Namespace(BaseSchema.class.getPackage(),
				LocalDate.of(2014, 1, 1));
		QualifiedName name = new QualifiedName(BaseSchema.class.getSimpleName(),
				namespace);

		/*
		 * Types
		 */
		Set<DataBindingType<?>> typeSet = new LinkedHashSet<>();

		DataBindingType<Enumeration> enumerationBaseType;
		typeSet.add(enumerationBaseType = dataTypeBuilder.configure(loader)
				.name("enumerationBase", namespace).isAbstract(true).isPrivate(true)
				.dataClass(Enumeration.class).create());

		DataBindingType<Object> primitive;
		typeSet.add(primitive = dataTypeBuilder
				.configure(loader)
				.name("primitive", namespace)
				.isAbstract(true)
				.isPrivate(true)
				.bindingClass(BufferedDataSource.class)
				.bindingStrategy(BindingStrategy.PROVIDED)
				.unbindingClass(DataTarget.class)
				.unbindingStrategy(UnbindingStrategy.PASS_TO_PROVIDED)
				.unbindingMethod("put")
				.providedUnbindingParameters("dataType", "this")
				.addChild(
						c -> c.data().name("dataType").type(enumerationBaseType)
								.isAbstract(true).isExtensible(true)
								.valueResolution(ValueResolution.REGISTRATION_TIME)
								.dataClass(DataType.class).outMethod("null")).create());

		primitives = new HashedMap<>();
		for (DataType<?> dataType : Enumeration.getConstants(DataType.class))
			primitives.put(
					dataType,
					dataTypeBuilder
							.configure(loader)
							.name(dataType.name(), namespace)
							.baseType(primitive)
							.dataClass((Class<?>) dataType.dataClass())
							.addChild(
									c -> c
											.data()
											.name("dataType")
											.isAbstract(true)
											.provideValue(
													new BufferingDataTarget().put(DataType.STRING,
															dataType.name()).buffer())).create());

		primitives.values().stream().forEach(typeSet::add);

		derivedTypes = new DerivedTypesImpl(loader, namespace, dataTypeBuilder,
				typeSet, primitives, enumerationBaseType);

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
