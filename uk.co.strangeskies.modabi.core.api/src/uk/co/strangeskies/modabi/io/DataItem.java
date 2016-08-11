/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
import java.util.Objects;
import java.util.function.Function;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.QualifiedName;
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
	private final Primitive<T> type;
	private final T data;

	TypedDataItem(Primitive<T> type, T data) {
		Objects.requireNonNull(type);
		
		this.type = type;
		this.data = data;
	}

	@Override
	public Primitive<T> type() {
		return type;
	}

	@Override
	public T data() {
		return data;
	}

	@Override
	public <U> DataItem<U> convert(Primitive<U> to) {
		if (to == type()) {
			@SuppressWarnings("unchecked")
			DataItem<U> conversion = (DataItem<U>) this;
			return conversion;
		}

		throw new ClassCastException(
				"Cannot convert type '" + type() + "' to type '" + to + "'");
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

	@Override
	public Primitive<String> type() {
		return Primitive.STRING;
	}

	@Override
	public String data() {
		return data;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <U> DataItem<U> convert(Primitive<U> to) {
		if (to == Primitive.QUALIFIED_NAME)
			return (DataItem<U>) DataItem.forDataOfType(Primitive.QUALIFIED_NAME,
					qualifiedNameParser.apply(data));

		try {
			return DataItem.forDataOfType(to, to.parse(data));
		} catch (ParseException e) {
			throw new ModabiException(
					t -> t.incompatibleTypes(type().dataClass(), to.dataClass()), e);
		}
	}
}

public interface DataItem<T> {
	static <T> DataItem<T> forDataOfType(Primitive<T> type, T data) {
		return new TypedDataItem<>(type, data);
	}

	static DataItem<?> forString(String data,
			Function<String, QualifiedName> qualifiedNameParser) {
		data = data.trim();

		for (Primitive<?> type : Enumeration.getConstants(Primitive.class))
			try {
				forStringStrict(type, data);
			} catch (Exception e) {}

		return new StringDataItem(data, qualifiedNameParser);
	}

	static <T> DataItem<T> forStringStrict(Primitive<T> type, String data)
			throws ParseException {
		return new TypedDataItem<>(type, type.strictParse(data));
	}

	<U> DataItem<U> convert(Primitive<U> to);

	default <U> U data(Primitive<U> type) {
		return convert(type).data();
	}

	Primitive<T> type();

	T data();
}
