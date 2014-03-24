package uk.co.strangeskies.modabi.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.ModelLoader;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.data.DataTypeBuilder;
import uk.co.strangeskies.modabi.data.DataTypes;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.Models;
import uk.co.strangeskies.modabi.model.building.ModelBuilder;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.processing.BindingStrategy;

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

		binaryType = dataType.configure().name("binary").dataClass(byte[].class)
				.create();
		typeSet.add(binaryType);

		stringType = dataType.configure().name("string").dataClass(String.class)
				.create();
		typeSet.add(stringType);

		integerType = dataType.configure().name("integer")
				.dataClass(BigInteger.class).create();
		typeSet.add(integerType);

		decimalType = dataType.configure().name("decimal")
				.dataClass(BigDecimal.class).create();
		typeSet.add(decimalType);

		intType = dataType.configure().name("int").dataClass(int.class).create();
		typeSet.add(intType);

		longType = dataType.configure().name("long").dataClass(long.class).create();
		typeSet.add(longType);

		floatType = dataType.configure().name("float").dataClass(float.class)
				.create();
		typeSet.add(floatType);

		doubleType = dataType.configure().name("double").dataClass(double.class)
				.create();
		typeSet.add(doubleType);

		booleanType = dataType.configure().name("boolean").dataClass(boolean.class)
				.create();
		typeSet.add(booleanType);

		/* Derived */

		classType = dataType.configure().name("class").dataClass(Class.class)
				.builderClass(Class.class)
				.bindingStrategy(BindingStrategy.STATIC_FACTORY)
				.addProperty(p -> p.type(stringType).inMethod("fromName")).create();
		typeSet.add(classType);

		enumType = dataType.configure().name("enum").dataClass(Enum.class).create();
		typeSet.add(enumType);

		rangeType = dataType.configure().name("range").dataClass(Range.class)
				.bindingStrategy(BindingStrategy.STATIC_FACTORY)
				.addProperty(p -> p.type(stringType)).create();
		typeSet.add(rangeType);

		referenceType = dataType.configure().name("reference")
				.dataClass(Object.class).create();
		typeSet.add(referenceType);

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

	@Override
	public DataType<Object> referenceType() {
		return referenceType;
	}

	@Override
	public Model<Object> includeModel() {
		return includeModel;
	}

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

	@Override
	public DataType<String> qualifiedNameType() {
		// TODO Auto-generated method stub
		return null;
	}
}
