package uk.co.strangeskies.modabi.io;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;

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

	public T parse(String string) throws ParseException {
		T data = tryStrictParse(string);

		if (data == null)
			data = tryParse(string);

		if (data == null)
			throw new ParseException(string, 0);

		return data;
	}

	/**
	 * Unambiguous string parser. Strings will be parsed as the given data types
	 * if and only if the following conditions are met, with surrounding
	 * whitespace ignored in each case:
	 *
	 * <ul>
	 * <li>BINARY: 0x#</li>
	 * <li>STRING: any string with normal escaping rules, surrounded by the
	 * following character at each end, ignoring quotes: "'"</li>
	 * <li>INTEGER: #I</li>
	 * <li>DECIMAL: #D</li>
	 * <li>INT: #i</li>
	 * <li>LONG: #l</li>
	 * <li>FLOAT: #f</li>
	 * <li>DOUBLE: #d</li>
	 * <li>BOOLEAN: true|false</li>
	 * <li>QUALIFIED_NAME: @[Qualified Name]</li>
	 * </ul>
	 *
	 * @param string
	 * @return
	 * @throws ParseException
	 */
	public T strictParse(String string) throws ParseException {
		T data = tryStrictParse(string);

		if (data == null)
			throw new ParseException(string, 0);

		return data;
	}

	private T tryParse(String string) {
		return null;
	}

	private T tryStrictParse(String string) {
		return null;
	}
}
