package uk.co.strangeskies.modabi.schema.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import uk.co.strangeskies.modabi.model.building.ModelBuilder;
import uk.co.strangeskies.modabi.model.nodes.DataNode.Value.ValueResolution;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.BaseSchema;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.SchemaBuilder;
import uk.co.strangeskies.modabi.schema.Schemata;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.ModelLoader;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.reference.DereferenceTarget;
import uk.co.strangeskies.modabi.schema.processing.reference.ReferenceSource;
import uk.co.strangeskies.modabi.schema.processing.reference.RelativeDereferenceTarget;
import uk.co.strangeskies.modabi.schema.processing.reference.RelativeReferenceSource;

public class BaseSchemaImpl implements BaseSchema {
	private class DerivedTypesImpl implements DerivedTypes {
		private final DataBindingType<QualifiedName> qualifiedNameType;
		private final DataBindingType<Object> relativeReferenceType;
		private final DataBindingType<Object> referenceType;
		private final DataBindingType<BufferedDataSource> bufferedDataType;

		@SuppressWarnings("rawtypes")
		private final DataBindingType<Class> classType;
		@SuppressWarnings("rawtypes")
		private final DataBindingType<Enum> enumType;
		@SuppressWarnings("rawtypes")
		private final DataBindingType<Range> rangeType;
		@SuppressWarnings("rawtypes")
		private final DataBindingType<List> listType;
		@SuppressWarnings("rawtypes")
		private final DataBindingType<Set> setType;

		public DerivedTypesImpl(DataBindingTypeBuilder builder,
				Set<DataBindingType<?>> typeSet,
				Map<DataType<?>, DataBindingType<?>> primitives) {
			qualifiedNameType = builder.configure().name("qualifiedName")
					.dataClass(QualifiedName.class).create();
			typeSet.add(qualifiedNameType);

			@SuppressWarnings("rawtypes")
			DataBindingType<Collection> collectionType = builder
					.configure()
					.name("collection")
					.dataClass(Collection.class)
					.isAbstract(true)
					.bindingStrategy(BindingStrategy.PROVIDED)
					.addChild(
							c -> c.data().id("element").inMethod("add").outMethod("this")
									.occurances(Range.create(0, null)).outMethodIterable(true))
					.create();
			typeSet.add(collectionType);

			listType = builder.configure().name("list").dataClass(List.class)
					.baseType(collectionType).create();
			typeSet.add(listType);

			setType = builder.configure().name("set").dataClass(Set.class)
					.baseType(collectionType).create();
			typeSet.add(setType);

			bufferedDataType = builder.configure().name("bufferedData")
					.dataClass(BufferedDataSource.class)
					.bindingClass(BufferedDataSource.class)
					.bindingStrategy(BindingStrategy.PROVIDED)
					.unbindingClass(DataTarget.class)
					.unbindingStrategy(UnbindingStrategy.ACCEPT_PROVIDED)
					.unbindingMethod("pipe").create();
			typeSet.add(bufferedDataType);

			DataBindingType<Object> referenceBaseType = builder
					.configure()
					.name("referenceBase")
					.dataClass(Object.class)
					.isAbstract(true)
					.bindingClass(ReferenceSource.class)
					.bindingStrategy(BindingStrategy.PROVIDED)
					.unbindingClass(DereferenceTarget.class)
					.unbindingStrategy(UnbindingStrategy.PASS_TO_PROVIDED)
					.addChild(
							c -> c.data().dataClass(Model.class).id("targetDomain")
									.valueResolution(ValueResolution.COMPILE_TIME))
					.addChild(
							c -> c.data().type(primitives.get(DataType.STRING)).id("id"))
					.addChild(c -> c.data().type(bufferedDataType).id("ref")).create();
			typeSet.add(referenceBaseType);

			DataBindingType<Object> relativeReferenceBaseType = builder
					.configure()
					.name("relativeReferenceBase")
					.dataClass(Object.class)
					.isAbstract(true)
					.bindingClass(RelativeReferenceSource.class)
					.bindingStrategy(BindingStrategy.PROVIDED)
					.unbindingClass(RelativeDereferenceTarget.class)
					.unbindingStrategy(UnbindingStrategy.PASS_TO_PROVIDED)
					.addChild(
							c -> c.data().id("targetDomain").dataClass(Model.class)
									.valueResolution(ValueResolution.COMPILE_TIME))
					.addChild(
							c -> c.data().type(primitives.get(DataType.INT))
									.id("parentLevel"))
					.addChild(
							c -> c
									.data()
									.type(listType)
									.id("elementIdList")
									.addChild(d -> d.data().id("element").type(qualifiedNameType)))
					.create();
			typeSet.add(relativeReferenceBaseType);

			referenceType = builder
					.configure()
					.name("reference")
					.baseType(referenceBaseType)
					.isAbstract(false)
					.addChild(
							c -> c
									.data()
									.id("targetDomain")
									.type(referenceBaseType)
									.provideValue(
											new BufferingDataTarget().put(DataType.STRING,
													"schema.modabi.strangeskies.co.uk:Model").buffer()))
					.addChild(c -> c.data().id("id")).addChild(c -> c.data().id("ref"))
					.create();
			typeSet.add(referenceType);

			relativeReferenceType = builder
					.configure()
					.name("relativeReference")
					.baseType(relativeReferenceBaseType)
					.isAbstract(false)
					.addChild(
							c -> c
									.data()
									.id("targetDomain")
									.type(referenceBaseType)
									.provideValue(
											new BufferingDataTarget().put(DataType.STRING,
													"schema.modabi.strangeskies.co.uk:Model").buffer()))
					.addChild(c -> c.data().id("parentLevel"))
					.addChild(c -> c.data().id("elementIdList")).create();
			typeSet.add(relativeReferenceType);

			classType = builder
					.configure()
					.name("class")
					.dataClass(Class.class)
					.bindingClass(Class.class)
					.bindingStrategy(BindingStrategy.STATIC_FACTORY)
					.addChild(
							p -> p.data().type(primitives.get(DataType.STRING)).id("name"))
					.create();
			typeSet.add(classType);

			enumType = builder
					.configure()
					.name("enum")
					.dataClass(Enum.class)
					.addChild(
							n -> n
									.inputSequence()
									.id("valueOf")
									.addChild(
											o -> o.data().id("enumType").outMethod("getClass")
													.type(referenceType).dataClass(Class.class))
									.addChild(
											o -> o.data().id("name")
													.type(primitives.get(DataType.STRING)))).create();
			typeSet.add(enumType);

			rangeType = builder
					.configure()
					.name("range")
					.dataClass(Range.class)
					.bindingStrategy(BindingStrategy.STATIC_FACTORY)
					.unbindingStrategy(UnbindingStrategy.STATIC_FACTORY)
					.addChild(
							p -> p.data().type(primitives.get(DataType.STRING)).id("string"))
					.create();
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
		public DataBindingType<Range> rangeType() {
			return rangeType;
		}

		@Override
		public DataBindingType<QualifiedName> qualifiedNameType() {
			return qualifiedNameType;
		}

		@Override
		public DataBindingType<Object> relativeReferenceType() {
			return relativeReferenceType;
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

		public BaseModelsImpl(ModelBuilder builder, Set<Model<?>> modelSet) {
			includeModel = builder.configure().id("include")
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
			DataBindingTypeBuilder dataTypeBuilder) {
		QualifiedName name = new QualifiedName(BaseSchema.class.getName(),
				new Namespace(BaseSchema.class.getPackage().getName()));

		/*
		 * Types
		 */
		Set<DataBindingType<?>> typeSet = new HashSet<>();

		DataBindingType<Object> primitive = dataTypeBuilder.configure()
				.name("primitive").bindingClass(BufferedDataSource.class)
				.bindingStrategy(BindingStrategy.PROVIDED)
				.unbindingClass(DataTarget.class)
				.unbindingStrategy(UnbindingStrategy.PASS_TO_PROVIDED)
				.unbindingMethod("put").create();
		typeSet.add(primitive);

		primitives = Enumeration
				.<DataType> getConstants(DataType.class)
				.stream()
				.collect(
						Collectors.toMap(
								t -> t,
								t -> dataTypeBuilder.configure().name(t.name())
										.dataClass((Class<?>) t.dataClass()).baseType(primitive)
										.create()));
		primitives.values().stream().forEach(typeSet::add);

		derivedTypes = new DerivedTypesImpl(dataTypeBuilder, typeSet, primitives);

		/*
		 * Models
		 */
		Set<Model<?>> modelSet = new LinkedHashSet<>();

		models = new BaseModelsImpl(modelBuilder, modelSet);

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
