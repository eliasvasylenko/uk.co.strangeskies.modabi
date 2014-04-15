package uk.co.strangeskies.modabi.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.data.DataTypeBuilder;
import uk.co.strangeskies.modabi.data.DataTypes;
import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.data.io.DataTarget;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.Models;
import uk.co.strangeskies.modabi.model.building.ModelBuilder;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.ModelLoader;
import uk.co.strangeskies.modabi.processing.UnbindingStrategy;

public class BaseSchemaImpl implements BaseSchema {
	private final Schema baseSchema;

	private class PrimitiveTypesImpl implements PrimitiveTypes {
		private final DataType<byte[]> binaryType;
		private final DataType<String> stringType;
		private final DataType<BigInteger> integerType;
		private final DataType<BigDecimal> decimalType;
		private final DataType<Integer> intType;
		private final DataType<Long> longType;
		private final DataType<Float> floatType;
		private final DataType<Double> doubleType;
		private final DataType<Boolean> booleanType;

		public PrimitiveTypesImpl(DataTypeBuilder builder, Set<DataType<?>> typeSet) {
			binaryType = primitiveType("binary", byte[].class, builder);
			typeSet.add(binaryType);

			stringType = primitiveType("string", String.class, builder);
			typeSet.add(stringType);

			integerType = primitiveType("integer", BigInteger.class, builder);
			typeSet.add(integerType);

			decimalType = primitiveType("decimal", BigDecimal.class, builder);
			typeSet.add(decimalType);

			intType = primitiveType("int", int.class, builder);
			typeSet.add(intType);

			longType = primitiveType("long", long.class, builder);
			typeSet.add(longType);

			floatType = primitiveType("float", float.class, builder);
			typeSet.add(floatType);

			doubleType = primitiveType("double", double.class, builder);
			typeSet.add(doubleType);

			booleanType = primitiveType("boolean", boolean.class, builder);
			typeSet.add(booleanType);
		}

		private <T> DataType<T> primitiveType(String name, Class<T> dataClass,
				DataTypeBuilder builder) {
			return builder.configure().name(name).dataClass(dataClass)
					.bindingClass(BufferedDataSource.class)
					.bindingStrategy(BindingStrategy.PROVIDED)
					.unbindingClass(DataTarget.class)
					.unbindingStrategy(UnbindingStrategy.PROVIDED).create();
		}

		@Override
		public DataType<byte[]> binaryType() {
			return binaryType;
		}

		@Override
		public DataType<String> stringType() {
			return stringType;
		}

		@Override
		public DataType<BigInteger> integerType() {
			return integerType;
		}

		@Override
		public DataType<BigDecimal> decimalType() {
			return decimalType;
		}

		@Override
		public DataType<Integer> intType() {
			return intType;
		}

		@Override
		public DataType<Long> longType() {
			return longType;
		}

		@Override
		public DataType<Float> floatType() {
			return floatType;
		}

		@Override
		public DataType<Double> doubleType() {
			return doubleType;
		}

		@Override
		public DataType<Boolean> booleanType() {
			return booleanType;
		}
	}

	private class BuiltInTypesImpl implements BuiltInTypes {
		private final DataType<QualifiedName> qualifiedNameType;
		private final DataType<Object> referenceType;
		private final DataType<BufferedDataSource> bufferedDataType;

		public BuiltInTypesImpl(DataTypeBuilder builder, Set<DataType<?>> typeSet) {
			qualifiedNameType = builder.configure().name("qualifiedName")
					.dataClass(QualifiedName.class).create();
			typeSet.add(qualifiedNameType);

			referenceType = builder.configure().name("reference")
					.dataClass(Object.class).create();
			typeSet.add(referenceType);

			bufferedDataType = builder.configure().name("bufferedData")
					.dataClass(BufferedDataSource.class).create();
		}

		@Override
		public DataType<QualifiedName> qualifiedNameType() {
			return qualifiedNameType;
		}

		@Override
		public DataType<Object> referenceType() {
			return referenceType;
		}

		@Override
		public DataType<BufferedDataSource> bufferedDataType() {
			return bufferedDataType;
		}
	}

	private class DerivedTypesImpl implements DerivedTypes {
		@SuppressWarnings("rawtypes")
		private final DataType<Class> classType;
		@SuppressWarnings("rawtypes")
		private final DataType<Enum> enumType;
		@SuppressWarnings("rawtypes")
		private final DataType<Range> rangeType;

		public DerivedTypesImpl(DataTypeBuilder dataType, Set<DataType<?>> typeSet,
				PrimitiveTypes primitives) {
			classType = dataType.configure().name("class").dataClass(Class.class)
					.bindingClass(Class.class)
					.bindingStrategy(BindingStrategy.STATIC_FACTORY)
					.addProperty(p -> p.type(primitives.stringType()).id("name"))
					.create();
			typeSet.add(classType);

			enumType = dataType.configure().name("enum").dataClass(Enum.class)
					.create();
			typeSet.add(enumType);

			rangeType = dataType.configure().name("range").dataClass(Range.class)
					.bindingStrategy(BindingStrategy.STATIC_FACTORY)
					.addProperty(p -> p.type(primitives.stringType()).id("string"))
					.create();
			typeSet.add(rangeType);
		}

		@Override
		@SuppressWarnings("rawtypes")
		public DataType<Class> classType() {
			return classType;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public DataType<Enum> enumType() {
			return enumType;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public DataType<Range> rangeType() {
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

	private final PrimitiveTypes primitiveTypes;
	private final BuiltInTypes builtInTypes;
	private final DerivedTypes derivedTypes;

	private final BaseModels models;

	public BaseSchemaImpl(SchemaBuilder schemaBuilder, ModelBuilder modelBuilder,
			DataTypeBuilder dataTypeBuilder) {
		QualifiedName name = new QualifiedName(BaseSchema.class.getName(),
				new Namespace(BaseSchema.class.getPackage().getName()));

		/*
		 * Types
		 */
		Set<DataType<?>> typeSet = new HashSet<>();

		primitiveTypes = new PrimitiveTypesImpl(dataTypeBuilder, typeSet);
		builtInTypes = new BuiltInTypesImpl(dataTypeBuilder, typeSet);
		derivedTypes = new DerivedTypesImpl(dataTypeBuilder, typeSet,
				primitiveTypes);

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

	@Override
	public PrimitiveTypes primitiveTypes() {
		return primitiveTypes;
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
	public DataTypes getDataTypes() {
		return baseSchema.getDataTypes();
	}

	@Override
	public Models getModels() {
		return baseSchema.getModels();
	}
}
