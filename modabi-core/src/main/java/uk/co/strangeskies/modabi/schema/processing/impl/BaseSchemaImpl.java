package uk.co.strangeskies.modabi.schema.processing.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import uk.co.strangeskies.modabi.schema.processing.ModelLoader;
import uk.co.strangeskies.modabi.schema.processing.RegistrationTimeTargetAdapter;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.ValueResolution;
import uk.co.strangeskies.modabi.schema.processing.reference.DereferenceTarget;
import uk.co.strangeskies.modabi.schema.processing.reference.ReferenceSource;

public class BaseSchemaImpl implements BaseSchema {
	private class DerivedTypesImpl implements DerivedTypes {
		private final DataBindingType<QualifiedName> qualifiedNameType;
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
		private final DataBindingType<List> listType;
		@SuppressWarnings("rawtypes")
		private final DataBindingType<Set> setType;

		public DerivedTypesImpl(
				DataLoader loader,
				DataBindingTypeBuilder builder,
				Set<DataBindingType<?>> typeSet,
				Map<DataType<?>, DataBindingType<?>> primitives,
				@SuppressWarnings("rawtypes") DataBindingType<Enumeration> enumerationBaseType) {
			qualifiedNameType = builder.configure(loader).name("qualifiedName")
					.dataClass(QualifiedName.class).create();
			typeSet.add(qualifiedNameType);

			@SuppressWarnings("rawtypes")
			DataBindingType<Collection> collectionType = builder
					.configure(loader)
					.name("collection")
					.dataClass(Collection.class)
					.isAbstract(true)
					.bindingStrategy(BindingStrategy.PROVIDED)
					.addChild(
							c -> c.data().name("element").inMethod("add").outMethod("this")
									.occurances(Range.create(0, null)).outMethodIterable(true))
					.create();
			typeSet.add(collectionType);

			listType = builder.configure(loader).name("list").dataClass(List.class)
					.baseType(collectionType).create();
			typeSet.add(listType);

			setType = builder.configure(loader).name("set").dataClass(Set.class)
					.baseType(collectionType).create();
			typeSet.add(setType);

			bufferedDataType = builder.configure(loader).name("bufferedData")
					.dataClass(BufferedDataSource.class)
					.bindingClass(BufferedDataSource.class)
					.bindingStrategy(BindingStrategy.PROVIDED)
					.unbindingClass(DataTarget.class)
					.unbindingStrategy(UnbindingStrategy.ACCEPT_PROVIDED)
					.unbindingMethod("pipe").create();
			typeSet.add(bufferedDataType);

			DataBindingType<Object> referenceBaseType = builder
					.configure(loader)
					.name("referenceBase")
					.dataClass(Object.class)
					.isAbstract(true)
					.isPrivate(true)
					.bindingClass(ReferenceSource.class)
					.bindingStrategy(BindingStrategy.PROVIDED)
					.unbindingClass(DereferenceTarget.class)
					.unbindingMethod("dereference")
					.unbindingStrategy(UnbindingStrategy.PASS_TO_PROVIDED)
					.providedUnbindingParameters("targetDomain", "id", "this")
					.addChild(
							d -> d.data().dataClass(Model.class).name("targetDomain")
									.valueResolution(ValueResolution.REGISTRATION_TIME)
									.inMethod("null").outMethod("null"))
					.addChild(
							d -> d.data().type(primitives.get(DataType.STRING)).name("id")
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
													.name("targetDomain")
													.outMethod("null")
													.valueResolution(ValueResolution.REGISTRATION_TIME)
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
																					DataType.STRING, "targetDomain")
																					.buffer()))
													.addChild(
															e -> e.inputSequence().name("providedValue")
																	.inMethodChained(true)))
									.addChild(
											d -> d
													.data()
													.dataClass(String.class)
													.name("id")
													.outMethod("null")
													.valueResolution(ValueResolution.REGISTRATION_TIME)
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
																					DataType.STRING, "id").buffer()))
													.addChild(
															e -> e.inputSequence().name("providedValue")
																	.inMethodChained(true)))
									.addChild(
											d -> d.data().type(bufferedDataType).name("ref")
													.outMethod("null"))).create();
			typeSet.add(referenceBaseType);

			referenceType = builder
					.configure(loader)
					.name("reference")
					.baseType(referenceBaseType)
					.isPrivate(false)
					.addChild(
							c -> c
									.data()
									.name("targetDomain")
									.type(referenceBaseType)
									.dataClass(Model.class)
									.addChild(
											d -> d
													.data()
													.name("targetDomain")
													.provideValue(
															new BufferingDataTarget().put(DataType.STRING,
																	"schema.modabi.strangeskies.co.uk:Model")
																	.buffer()))
									.addChild(
											d -> d
													.data()
													.name("id")
													.provideValue(
															new BufferingDataTarget().put(DataType.STRING,
																	"name").buffer()))).create();
			typeSet.add(referenceType);

			classType = builder.configure(loader).name("class")
					.dataClass(Class.class)
					.bindingStrategy(BindingStrategy.STATIC_FACTORY).addChild(
					// TODO outMethod not being carried through to unbinding process...
							p -> p.data().type(primitives.get(DataType.STRING)).name("name"))
					.create();
			typeSet.add(classType);

			enumType = builder
					.configure(loader)
					.name("enum")
					.dataClass(Enum.class)
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
													.type(primitives.get(DataType.STRING)))).create();
			typeSet.add(enumType);

			enumerationType = builder
					.configure(loader)
					.name("enumeration")
					.baseType(enumerationBaseType)
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
													.type(primitives.get(DataType.STRING)))).create();
			typeSet.add(enumType);

			rangeType = builder
					.configure(loader)
					.name("range")
					.dataClass(Range.class)
					.bindingStrategy(BindingStrategy.STATIC_FACTORY)
					.unbindingStrategy(UnbindingStrategy.STATIC_FACTORY)
					.unbindingClass(String.class)
					.unbindingFactoryClass(Range.class)
					.addChild(
							p -> p.data().type(primitives.get(DataType.STRING))
									.name("string")).create();
			typeSet.add(rangeType);
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
		public DataBindingType<QualifiedName> qualifiedNameType() {
			return qualifiedNameType;
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

		public BaseModelsImpl(DataLoader loader, ModelBuilder builder,
				Set<Model<?>> modelSet) {
			includeModel = builder.configure(loader).name("include")
					.bindingClass(ModelLoader.class).dataClass(Object.class).create();
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public BaseSchemaImpl(SchemaBuilder schemaBuilder, ModelBuilder modelBuilder,
			DataBindingTypeBuilder dataTypeBuilder, DataLoader loader) {
		QualifiedName name = new QualifiedName(BaseSchema.class.getName(),
				new Namespace(BaseSchema.class.getPackage().getName()));

		/*
		 * Types
		 */
		Set<DataBindingType<?>> typeSet = new HashSet<>();

		DataBindingType<Enumeration> enumerationBaseType = dataTypeBuilder
				.configure(loader).name("enumerationBase").isAbstract(true)
				.isPrivate(true).dataClass(Enumeration.class).create();
		typeSet.add(enumerationBaseType);

		DataBindingType<Object> primitive = dataTypeBuilder
				.configure(loader)
				.name("primitive")
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
								.dataClass(DataType.class).outMethod("null")).create();
		typeSet.add(primitive);

		primitives = Enumeration
				.<DataType> getConstants(DataType.class)
				.stream()
				.collect(
						Collectors.toMap(
								Function.identity(),
								t -> dataTypeBuilder
										.configure(loader)
										.name(t.name())
										.baseType(primitive)
										.isAbstract(false)
										.isPrivate(false)
										.dataClass((Class<?>) t.dataClass())
										.addChild(
												c -> c
														.data()
														.name("dataType")
														.provideValue(
																new BufferingDataTarget().put(DataType.STRING,
																		t.name()).buffer())).create()));
		primitives.values().stream().forEach(typeSet::add);

		derivedTypes = new DerivedTypesImpl(loader, dataTypeBuilder, typeSet,
				primitives, enumerationBaseType);

		/*
		 * Models
		 */
		Set<Model<?>> modelSet = new LinkedHashSet<>();

		models = new BaseModelsImpl(loader, modelBuilder, modelSet);

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
