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
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.utility.Copyable;

public interface DataSource extends Copyable<DataSource> {
	DataStreamState currentState();

	DataSource reset();

	int index();

	default boolean isComplete() {
		return index() == size();
	}

	default <T> T get(Primitive<T> type) {
		return get().data(type);
	}

	DataItem<?> get();

	default Stream<DataItem<?>> stream() {
		return Stream.generate(this::get).limit(size() - index()).map(i -> (DataItem<?>) i);
	}

	default <T> T peek(Primitive<T> type) {
		return peek().data(type);
	}

	DataItem<?> peek();

	<T extends DataTarget> T pipe(T target, int items);

	default <T extends DataTarget> T pipeNext(T target) {
		return pipe(target, 1);
	}

	int size();

	default <T extends DataTarget> T pipe(T target) {
		return pipe(target, size() - index());
	}

	static DataSource parseString(String string, Function<String, QualifiedName> qualifiedNameParser) {
		List<DataItem<?>> dataItemList = new ArrayList<>();

		Function<String, String> removeEscapes = s -> {
			char[] characters = s.toCharArray();

			int output = 0;
			char previous = characters[0];
			char character = ' ';
			for (int i = 1; i < characters.length; i++) {
				character = characters[i];

				if (previous != '\\' && character != ',') {
					characters[output++] = previous;
				}

				previous = character;
			}
			characters[output++] = character;
			return new String(characters, 0, output);
		};

		boolean wasEscape = false;
		boolean splitContainsEscapes = false;

		int startSplit = 0;
		int endSplit = 0;

		for (endSplit = 0; endSplit < string.length(); endSplit++) {
			char c = string.charAt(endSplit);

			switch (c) {
			case '\\':
				wasEscape = true;
				break;
			case ',':
				if (wasEscape) {
					splitContainsEscapes = true;
				} else {
					String item = string.substring(startSplit, endSplit);
					if (splitContainsEscapes) {
						item = removeEscapes.apply(item);
						splitContainsEscapes = false;
					}
					dataItemList.add(DataItem.forString(item.trim(), qualifiedNameParser));

					startSplit = endSplit + 1;
					endSplit = startSplit + 1;
				}
			default:
				wasEscape = false;
			}
		}

		String item = string.substring(startSplit, endSplit);
		if (splitContainsEscapes) {
			item = removeEscapes.apply(item);
			splitContainsEscapes = false;
		}
		dataItemList.add(DataItem.forString(item.trim(), qualifiedNameParser));

		return forDataItems(dataItemList);
	}

	static DataSource forDataItems(List<DataItem<?>> dataItemList) {
		return new DataSourceDecorator(new RepeatingDataSource(dataItemList, 0, dataItemList.size()));
	}

	static <T> DataSource repeating(Primitive<T> type, T data, int times) {
		return repeating(DataItem.forDataOfType(type, data), times);
	}

	static <T> DataSource repeating(DataItem<?> item, int times) {
		return new DataSourceDecorator(new RepeatingDataSource(item, times));
	}

	static <T> DataSource single(Primitive<T> type, T data) {
		return single(DataItem.forDataOfType(type, data));
	}

	static DataSource single(DataItem<?> item) {
		return new DataSourceDecorator(new RepeatingDataSource(item, 1));
	}

	class RepeatingDataSource implements DataSource {
		private final List<DataItem<?>> list;
		private int index;
		private final int size;

		private RepeatingDataSource(DataItem<?> item, int size) {
			this(item, 0, size);
		}

		private RepeatingDataSource(DataItem<?> item, int index, int size) {
			this(Arrays.asList(item), index, size);
		}

		private RepeatingDataSource(List<DataItem<?>> list, int index, int size) {
			this.list = list;
			this.index = index;
			this.size = size;
		}

		@Override
		public boolean equals(Object that) {
			if (!(that instanceof DataSource))
				return false;

			DataSource thatDataSource = ((DataSource) that).copy().reset();
			DataItem<?> item;
			while ((item = thatDataSource.get()) != null)
				if (!item.equals(this.list))
					return false;

			return thatDataSource.index() == index && thatDataSource.size() == size;
		}

		@Override
		public int hashCode() {
			return list.hashCode() ^ index ^ size;
		}

		@Override
		public int index() {
			return index;
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public DataItem<?> get() {
			return list.get(index++ % list.size());
		}

		@Override
		public DataItem<?> peek() {
			return list.get(index % list.size());
		}

		@Override
		public <U extends DataTarget> U pipe(U target, int items) {
			if (items + index() > size)
				throw new ArrayIndexOutOfBoundsException(size);

			if (items < 0)
				throw new ArrayIndexOutOfBoundsException(items);

			for (int i = 0; i < items; i++)
				target.put(list.get((index + i) % list.size()));

			index += items;

			return target;
		}

		@Override
		public DataStreamState currentState() {
			return null;
		}

		@Override
		public DataSource reset() {
			index = 0;
			return this;
		}

		@Override
		public DataSource copy() {
			return new RepeatingDataSource(list, index, size);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder("[ ");

			for (int i = 0; i < size; i++) {
				if (i > 0)
					builder.append(", ");

				if (i == index)
					builder.append("[");

				builder.append(list.get(i % list.size()));

				if (i == index)
					builder.append("]");

			}

			return builder.append(" ]").toString();
		}
	}
}
