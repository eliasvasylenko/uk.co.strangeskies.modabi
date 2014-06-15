package uk.co.strangeskies.modabi.schema.impl;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.DataBindingTypeBuilder;
import uk.co.strangeskies.modabi.data.DataBindingTypes;
import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.data.io.DataTarget;
import uk.co.strangeskies.modabi.data.io.DataType;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.Models;
import uk.co.strangeskies.modabi.model.building.ModelBuilder;
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
	private class BuiltInTypesImpl implements BuiltInTypes {
		private final DataBindingType<QualifiedName> qualifiedNameType;
		private final DataBindingType<Object> relativeReferenceType;
		private final DataBindingType<Object> referenceType;
		private final DataBindingType<BufferedDataSource> bufferedDataType;

		@SuppressWarnings("unchecked")
		public BuiltInTypesImpl(DataBindingTypeBuilder builder,
				Set<DataBindingType<?>> typeSet,
				Map<DataType<?>, DataBindingType<?>> primitives) {
			qualifiedNameType = builder.configure().name("qualifiedName")
					.dataClass(QualifiedName.class).create();
			typeSet.add(qualifiedNameType);

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
							c -> c.data().type(primitives.get(DataType.INT))
									.id("parentLevel"))
					.addChild(
							c -> c.data().type(qualifiedNameType).id("elementId")
									.occurances(Range.create(0, null))).create();
			typeSet.add(relativeReferenceBaseType);

			relativeReferenceType = builder.configure().name("relativeReference")
					.baseType(relativeReferenceBaseType).isAbstract(false).create();
			typeSet.add(relativeReferenceType);

			DataBindingType<Object> referenceBaseType = builder
					.configure()
					.name("referenceBase")
					.dataClass(Object.class)
					.isAbstract(true)
					.bindingClass(ReferenceSource.class)
					.bindingStrategy(BindingStrategy.PROVIDED)
					.unbindingClass(DereferenceTarget.class)
					.unbindingStrategy(UnbindingStrategy.PASS_TO_PROVIDED)
					.addChild(c -> c.data().dataClass(Model.class).id("targetDomain"))
					.addChild(
							c -> c.data().type(primitives.get(DataType.STRING))
									.id("targetIdDomain"))
					.addChild(c -> c.data().type(qualifiedNameType).id("id")).create();
			typeSet.add(referenceBaseType);

			referenceType = builder.configure().name("reference")
					.baseType(referenceBaseType).isAbstract(false)
					.addChild(c -> c.data().type(referenceBaseType).id("targetDomain"))
					.addChild(c -> c.data().id("targetIdDomain"))
					.addChild(c -> c.data().id("id")).create();
			typeSet.add(referenceType);

			bufferedDataType = builder.configure().name("bufferedData")
					.dataClass(BufferedDataSource.class)
					.bindingClass(BufferedDataSource.class)
					.bindingStrategy(BindingStrategy.PROVIDED)
					.unbindingClass(DataTarget.class)
					.unbindingStrategy(UnbindingStrategy.ACCEPT_PROVIDED)
					.unbindingMethod("pipe").create();
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
	}

	private class DerivedTypesImpl implements DerivedTypes {
		@SuppressWarnings("rawtypes")
		private final DataBindingType<Class> classType;
		@SuppressWarnings("rawtypes")
		private final DataBindingType<Enum> enumType;
		@SuppressWarnings("rawtypes")
		private final DataBindingType<Range> rangeType;

		public DerivedTypesImpl(DataBindingTypeBuilder dataType,
				Set<DataBindingType<?>> typeSet,
				Map<DataType<?>, DataBindingType<?>> primitives,
				BuiltInTypes builtInTypes) {
			classType = dataType
					.configure()
					.name("class")
					.dataClass(Class.class)
					.bindingClass(Class.class)
					.bindingStrategy(BindingStrategy.STATIC_FACTORY)
					.addChild(
							p -> p.data().type(primitives.get(DataType.STRING)).id("name"))
					.create();
			typeSet.add(classType);

			enumType = dataType
					.configure()
					.name("enum")
					.dataClass(Enum.class)
					.addChild(
							n -> n
									.inputSequence()
									.id("valueOf")
									.addChild(
											o -> o.data().id("enumType").outMethod("getClass")
													.type(builtInTypes.referenceType())
													.dataClass(Class.class))
									.addChild(
											o -> o.data().id("name")
													.type(primitives.get(DataType.STRING)))).create();
			typeSet.add(enumType);

			rangeType = dataType
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
	private final BuiltInTypes builtInTypes;
	private final DerivedTypes derivedTypes;

	private final BaseModels models;

	public BaseSchemaImpl(SchemaBuilder schemaBuilder, ModelBuilder modelBuilder,
			DataBindingTypeBuilder dataTypeBuilder) {
		QualifiedName name = new QualifiedName(BaseSchema.class.getName(),
				new Namespace(BaseSchema.class.getPackage().getName()));

		/*
		 * Types
		 */
		Set<DataBindingType<?>> typeSet = new HashSet<>();

		primitives = DataType
				.types()
				.stream()
				.collect(
						Collectors.toMap(t -> t,
								t -> primitive(dataTypeBuilder, typeSet, t)));
		builtInTypes = new BuiltInTypesImpl(dataTypeBuilder, typeSet, primitives);
		derivedTypes = new DerivedTypesImpl(dataTypeBuilder, typeSet, primitives,
				builtInTypes);

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

	private <T> DataBindingType<T> primitive(DataBindingTypeBuilder builder,
			Set<DataBindingType<?>> typeSet, DataType<T> type) {
		DataBindingType<T> primitive = builder.configure().name(type.name())
				.dataClass(type.dataClass()).bindingClass(BufferedDataSource.class)
				.bindingStrategy(BindingStrategy.PROVIDED)
				.unbindingClass(DataTarget.class)
				.unbindingStrategy(UnbindingStrategy.PASS_TO_PROVIDED)
				.unbindingMethod("put").create();

		typeSet.add(primitive);
		return primitive;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> DataBindingType<T> primitiveType(DataType<T> type) {
		return (DataBindingType<T>) primitives.get(type);
	}

	@Override
	public BuiltInTypes builtInTypes() {
		return builtInTypes;
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
