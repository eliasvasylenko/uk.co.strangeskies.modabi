package uk.co.strangeskies.modabi.data.io;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DataType<T> {
	public static final DataType<byte[]> BYTES = create("binary", byte[].class);
	public static final DataType<String> STRING = create("string", String.class);

	public static final DataType<BigInteger> INTEGER = create("integer",
			BigInteger.class);
	public static final DataType<BigDecimal> DECIMAL = create("decimal",
			BigDecimal.class);

	public static final DataType<Integer> INT = create("int", int.class);
	public static final DataType<Long> LONG = create("long", long.class);
	public static final DataType<Float> FLOAT = create("float", float.class);
	public static final DataType<Double> DOUBLE = create("double", double.class);

	public static final DataType<Boolean> BOOLEAN = create("boolean",
			boolean.class);

	private static final Set<DataType<?>> TYPES = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList(BYTES, STRING, INTEGER,
					DECIMAL, INT, LONG, FLOAT, DOUBLE, BOOLEAN)));

	private static <T> DataType<T> create(String name, Class<T> dataClass) {
		return new DataType<>(name, dataClass);
	}

	public static Set<DataType<?>> types() {
		return TYPES;
	}

	private final String name;
	private final Class<T> dataClass;

	private DataType(String name, Class<T> dataClass) {
		this.name = name;
		this.dataClass = dataClass;
	}

	public String name() {
		return name;
	}

	public Class<T> dataClass() {
		return dataClass;
	}
}
