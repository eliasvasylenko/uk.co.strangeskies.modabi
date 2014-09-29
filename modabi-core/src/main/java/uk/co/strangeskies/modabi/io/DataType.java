package uk.co.strangeskies.modabi.io;

import java.math.BigDecimal;
import java.math.BigInteger;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.utilities.Enumeration;

public class DataType<T> extends Enumeration<DataType<T>> {
	public static final DataType<byte[]> BINARY = new DataType<>("binary",
			byte[].class);
	public static final DataType<String> STRING = new DataType<>("string",
			String.class);

	public static final DataType<BigInteger> INTEGER = new DataType<>("integer",
			BigInteger.class);
	public static final DataType<BigDecimal> DECIMAL = new DataType<>("decimal",
			BigDecimal.class);

	public static final DataType<Integer> INT = new DataType<>("int", int.class);
	public static final DataType<Long> LONG = new DataType<>("long", long.class);
	public static final DataType<Float> FLOAT = new DataType<>("float",
			float.class);
	public static final DataType<Double> DOUBLE = new DataType<>("double",
			double.class);

	public static final DataType<Boolean> BOOLEAN = new DataType<>("boolean",
			boolean.class);

	public static final DataType<QualifiedName> QUALIFIED_NAME = new DataType<>(
			"qualifiedName", QualifiedName.class);

	private final Class<T> dataClass;

	private DataType(String name, Class<T> dataClass) {
		super(name);
		this.dataClass = dataClass;
	}

	public Class<T> dataClass() {
		return dataClass;
	}
}
