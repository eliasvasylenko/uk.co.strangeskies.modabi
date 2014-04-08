package uk.co.strangeskies.modabi;

import java.math.BigDecimal;
import java.math.BigInteger;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public interface BaseSchema extends Schema {
	public interface PrimitiveTypes {
		DataType<byte[]> binaryType();

		DataType<String> stringType();

		DataType<BigInteger> integerType();

		DataType<BigDecimal> decimalType();

		DataType<Integer> intType();

		DataType<Long> longType();

		DataType<Float> floatType();

		DataType<Double> doubleType();

		DataType<Boolean> booleanType();
	}

	public interface BuiltInTypes {
		DataType<Object> referenceType();

		DataType<QualifiedName> qualifiedNameType();

		DataType<DataInterface> bufferedDataType();
	}

	public interface DerivedTypes {
		@SuppressWarnings("rawtypes")
		DataType<Range> rangeType();

		@SuppressWarnings("rawtypes")
		DataType<Enum> enumType();

		@SuppressWarnings("rawtypes")
		DataType<Class> classType();
	}

	public interface BaseModels {
		Model<Object> includeModel();
	}

	PrimitiveTypes primitiveTypes();

	BuiltInTypes builtInTypes();

	DerivedTypes derivedTypes();

	BaseModels models();
}
