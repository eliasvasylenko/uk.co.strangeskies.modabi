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

import java.text.ParseException;
import java.util.function.Function;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.utilities.Enumeration;

abstract class AbstractDataItem<T> implements DataItem<T> {
	@Override
	public boolean equals(Object that) {
		if (!(that instanceof DataItem))
			return false;

		DataItem<?> thatDataItem = (DataItem<?>) that;

		return type() == thatDataItem.type() && data().equals(thatDataItem.data());
	}

	@Override
	public int hashCode() {
		return type().hashCode() + data().hashCode();
	}

	@Override
	public String toString() {
		return type() + ": " + data();
	}
}

class TypedDataItem<T> extends AbstractDataItem<T> {
	private final DataType<T> type;
	private final T data;

	TypedDataItem(DataType<T> type, T data) {
		this.type = type;
		this.data = data;
	}

	public DataType<T> type() {
		return type;
	}

	public T data() {
		return data;
	}

	@Override
	public <U> DataItem<U> convert(DataType<U> to) {
		if (to == type()) {
			@SuppressWarnings("unchecked")
			DataItem<U> conversion = (DataItem<U>) this;
			return conversion;
		}

		throw new ClassCastException("Cannot convert type '" + type()
				+ "' to type '" + to + "'.");
	}
}

class StringDataItem extends AbstractDataItem<String> {
	private final String data;
	private final Function<String, QualifiedName> qualifiedNameParser;

	StringDataItem(String data,
			Function<String, QualifiedName> qualifiedNameParser) {
		this.data = data;
		this.qualifiedNameParser = qualifiedNameParser;
	}

	public DataType<String> type() {
		return DataType.STRING;
	}

	public String data() {
		return data;
	}

	@SuppressWarnings("unchecked")
	public <U> DataItem<U> convert(DataType<U> to) {
		if (to == DataType.QUALIFIED_NAME)
			return (DataItem<U>) DataItem.forDataOfType(DataType.QUALIFIED_NAME,
					qualifiedNameParser.apply(data));

		try {
			return DataItem.forDataOfType(to, to.parse(data));
		} catch (ParseException e) {
			throw new IllegalArgumentException("Cannot convert type '" + type()
					+ "' to type '" + to + "'.", e);
		}
	}
}

public interface DataItem<T> {
	static <T> DataItem<T> forDataOfType(DataType<T> type, T data) {
		return new TypedDataItem<T>(type, data);
	}

	static DataItem<?> forString(String data,
			Function<String, QualifiedName> qualifiedNameParser) {
		data = data.trim();

		for (DataType<?> type : Enumeration.getConstants(DataType.class))
			try {
				forStringStrict(type, data);
			} catch (ParseException e) {
			}

		;

		return new StringDataItem(data, qualifiedNameParser);
	}

	static <T> DataItem<T> forStringStrict(DataType<T> type, String data)
			throws ParseException {
		return new TypedDataItem<>(type, type.strictParse(data));
	}

	<U> DataItem<U> convert(DataType<U> to);

	default <U> U data(DataType<U> type) {
		return convert(type).data();
	}

	DataType<T> type();

	T data();
}
