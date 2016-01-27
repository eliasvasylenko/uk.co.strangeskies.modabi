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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import uk.co.strangeskies.modabi.QualifiedName;

public interface DataTarget {
	public default <T> DataTarget put(Primitive<T> type, T data) {
		return put(DataItem.forDataOfType(type, data));
	}

	public <T> DataTarget put(DataItem<T> item);

	public DataStreamState currentState();

	public void terminate();

	static DataTarget composeString(Consumer<String> resultConsumer,
			Function<QualifiedName, String> qualifiedNameFormat) {
		return new DataTargetDecorator(new DataTarget() {
			private boolean terminated;
			private boolean compound;

			private final StringBuilder stringBuilder = new StringBuilder();

			private void next(Object value) {
				if (compound)
					stringBuilder.append(", ");
				else
					compound = true;
				stringBuilder.append(value);
			}

			@Override
			public <T> DataTarget put(DataItem<T> item) {
				if (terminated)
					throw new ModabiIOException();

				if (item.type() == Primitive.QUALIFIED_NAME) {
					next(qualifiedNameFormat.apply((QualifiedName) item.data()));
				} else
					next(item.data());
				return this;
			}

			@Override
			public void terminate() {
				resultConsumer.accept(stringBuilder.toString());

				terminated = true;
			}

			@Override
			public DataStreamState currentState() {
				return null;
			}
		});
	}

	static DataTarget composeList(Consumer<List<Object>> resultConsumer,
			Function<QualifiedName, String> qualifiedNameFormat) {
		return new DataTargetDecorator(new DataTarget() {
			private boolean terminated;

			private final List<Object> result = new ArrayList<>();

			private void next(Object value) {
				result.add(value);
			}

			@Override
			public <T> DataTarget put(DataItem<T> item) {
				if (terminated)
					throw new ModabiIOException();

				if (item.type() == Primitive.QUALIFIED_NAME) {
					next(qualifiedNameFormat.apply((QualifiedName) item.data()));
				} else
					next(item.data());
				return this;
			}

			@Override
			public void terminate() {
				resultConsumer.accept(result);

				terminated = true;
			}

			@Override
			public DataStreamState currentState() {
				return null;
			}
		});
	}
}
