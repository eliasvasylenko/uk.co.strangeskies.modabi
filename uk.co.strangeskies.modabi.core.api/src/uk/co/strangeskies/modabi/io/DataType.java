/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.api.
 *
 * uk.co.strangeskies.modabi.core.api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.api.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.io;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.function.Function;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.utilities.Enumeration;

public class DataType<T> extends Enumeration<DataType<T>> {
	public static final DataType<byte[]> BINARY = new DataType<>("binary",
			byte[].class, s -> null);

	public static final DataType<String> STRING = new DataType<>("string",
			String.class, Function.identity());

	public static final DataType<BigInteger> INTEGER = new DataType<>("integer",
			BigInteger.class, s -> null);

	public static final DataType<BigDecimal> DECIMAL = new DataType<>("decimal",
			BigDecimal.class, s -> null);

	public static final DataType<Integer> INT = new DataType<>("int", int.class,
			s -> null);

	public static final DataType<Long> LONG = new DataType<>("long", long.class,
			s -> null);

	public static final DataType<Float> FLOAT = new DataType<>("float",
			float.class, s -> null);

	public static final DataType<Double> DOUBLE = new DataType<>("double",
			double.class, s -> null);

	public static final DataType<Boolean> BOOLEAN = new DataType<>("boolean",
			boolean.class, s -> Boolean.parseBoolean(s));

	public static final DataType<QualifiedName> QUALIFIED_NAME = new DataType<>(
			"qualifiedName", QualifiedName.class, s -> null);

	private final Class<T> dataClass;
	private final Function<String, T> parse;
	private final Function<String, T> strictParse;

	private DataType(String name, Class<T> dataClass, Function<String, T> parse) {
		this(name, dataClass, parse, parse);
	}

	private DataType(String name, Class<T> dataClass, Function<String, T> parse,
			Function<String, T> strictParse) {
		super(name);
		this.dataClass = dataClass;
		this.parse = parse;
		this.strictParse = strictParse;
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
		string = string.trim();
		return parse.apply(string);
	}

	private T tryStrictParse(String string) {
		string = string.trim();
		return strictParse.apply(string);
	}
}
