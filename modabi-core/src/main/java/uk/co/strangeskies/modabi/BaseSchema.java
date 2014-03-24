package uk.co.strangeskies.modabi;

import java.math.BigDecimal;
import java.math.BigInteger;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.model.Model;

public interface BaseSchema extends Schema {
	/* Primitive Types */

	DataType<byte[]> binaryType();

	DataType<String> stringType();

	DataType<BigInteger> integerType();

	DataType<BigDecimal> decimalType();

	DataType<Integer> intType();

	DataType<Long> longType();

	DataType<Float> floatType();

	DataType<Double> doubleType();

	DataType<Boolean> booleanType();

	/* Built-In Types */

	DataType<Object> referenceType();

	DataType<String> qualifiedNameType();

	/* Derived Types */

	@SuppressWarnings("rawtypes")
	DataType<Range> rangeType();

	@SuppressWarnings("rawtypes")
	DataType<Class> classType();

	/* Models */

	Model<Object> includeModel();
}
