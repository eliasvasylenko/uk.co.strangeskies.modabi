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
import uk.co.strangeskies.modabi.data.DataSink;
import uk.co.strangeskies.modabi.data.DataSource;
import uk.co.strangeskies.modabi.data.DataTypeBuilder;
import uk.co.strangeskies.modabi.data.DataTypes;
import uk.co.strangeskies.modabi.data.DataType;
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

	private final DataType<byte[]> binaryType;
	private final DataType<String> stringType;
	private final DataType<BigInteger> integerType;
	private final DataType<BigDecimal> decimalType;
	private final DataType<Integer> intType;
	private final DataType<Long> longType;
	private final DataType<Float> floatType;
	private final DataType<Double> doubleType;
	private final DataType<Boolean> booleanType;

	@SuppressWarnings("rawtypes")
	private final DataType<Class> classType;
	@SuppressWarnings("rawtypes")
	private final DataType<Enum> enumType;
	@SuppressWarnings("rawtypes")
	private final DataType<Range> rangeType;

	private final DataType<QualifiedName> qualifiedNameType;
	private final DataType<Object> referenceType;

	private final Model<Object> includeModel;

	public BaseSchemaImpl(SchemaBuilder schema, ModelBuilder model,
			DataTypeBuilder dataType) {
		QualifiedName name = new QualifiedName(BaseSchema.class.getName(),
				new Namespace(BaseSchema.class.getPackage().getName()));

		/*
		 * Types
		 */
		Set<DataType<?>> typeSet = new HashSet<>();

		/* Primitive */

		binaryType = primitiveType("binary", byte[].class, dataType);
		typeSet.add(binaryType);

		stringType = primitiveType("string", String.class, dataType);
		typeSet.add(stringType);

		integerType = primitiveType("integer", BigInteger.class, dataType);
		typeSet.add(integerType);

		decimalType = primitiveType("decimal", BigDecimal.class, dataType);
		typeSet.add(decimalType);

		intType = primitiveType("int", int.class, dataType);
		typeSet.add(intType);

		longType = primitiveType("long", long.class, dataType);
		typeSet.add(longType);

		floatType = primitiveType("float", float.class, dataType);
		typeSet.add(floatType);

		doubleType = primitiveType("double", double.class, dataType);
		typeSet.add(doubleType);

		booleanType = primitiveType("boolean", boolean.class, dataType);
		typeSet.add(booleanType);

		/* Built-In */

		qualifiedNameType = dataType.configure().name("qualifiedName")
				.dataClass(QualifiedName.class).create();
		typeSet.add(qualifiedNameType);

		referenceType = dataType.configure().name("reference")
				.dataClass(Object.class).create();
		typeSet.add(referenceType);

		/* Derived */

		classType = dataType.configure().name("class").dataClass(Class.class)
				.bindingClass(Class.class)
				.bindingStrategy(BindingStrategy.STATIC_FACTORY)
				.addProperty(p -> p.type(stringType).id("name")).create();
		typeSet.add(classType);

		enumType = dataType.configure().name("enum").dataClass(Enum.class).create();
		typeSet.add(enumType);

		rangeType = dataType.configure().name("range").dataClass(Range.class)
				.bindingStrategy(BindingStrategy.STATIC_FACTORY)
				.addProperty(p -> p.type(stringType).id("string")).create();
		typeSet.add(rangeType);

		/*
		 * Models
		 */
		Set<Model<?>> modelSet = new LinkedHashSet<>();

		includeModel = model.configure().id("include")
				.builderClass(ModelLoader.class).dataClass(Object.class).create();
		modelSet.add(includeModel);

		/*
		 * Schema
		 */
		baseSchema = schema.configure().qualifiedName(name).types(typeSet)
				.models(modelSet).create();
	}

	/* Primitive */

	private <T> DataType<T> primitiveType(String name,
			Class<T> dataClass, DataTypeBuilder builder) {
		return builder.configure().name(name).dataClass(dataClass)
				.bindingClass(DataSource.class)
				.bindingStrategy(BindingStrategy.REQUIRE_PROVIDED)
				.unbindingClass(DataSink.class)
				.unbindingStrategy(UnbindingStrategy.COMPOSE).create();
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

	/* Built-In */

	@Override
	public DataType<QualifiedName> qualifiedNameType() {
		return qualifiedNameType;
	}

	@Override
	public DataType<Object> referenceType() {
		return referenceType;
	}

	/* Derived */

	@Override
	@SuppressWarnings("rawtypes")
	public DataType<Class> classType() {
		return classType;
	}

	@SuppressWarnings("rawtypes")
	public DataType<Enum> enumType() {
		return enumType;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public DataType<Range> rangeType() {
		return rangeType;
	}

	/* Models */

	@Override
	public Model<Object> includeModel() {
		return includeModel;
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
