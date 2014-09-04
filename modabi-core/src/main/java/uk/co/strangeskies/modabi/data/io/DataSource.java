package uk.co.strangeskies.modabi.data.io;

import uk.co.strangeskies.utilities.Copyable;

public interface DataSource extends Copyable<DataSource> {
	DataStreamState currentState();

	DataSource reset();

	int index();

	default <T> T get(DataType<T> type) {
		return get().data(type);
	}

	DataItem<?> get();

	<T extends DataTarget> T pipe(T target, int items);

	default <T extends DataTarget> T pipeNext(T target) {
		return pipe(target, 1);
	}

	int size();

	default <T extends DataTarget> T pipe(T target) {
		return pipe(target, size());
	}

	static DataSource parseString(String content) {
		return null; // TODO
	}

	static <T> DataSource repeating(DataType<T> type, T data, int times) {
		return repeating(new DataItem<>(type, data), times);
	}

	static <T> DataSource repeating(DataItem<?> item, int times) {
		return new DataSourceDecorator(new RepeatingDataSource(item, times));
	}

	static <T> DataSource single(DataType<T> type, T data) {
		return single(new DataItem<>(type, data));
	}

	static DataSource single(DataItem<?> item) {
		return new DataSourceDecorator(new RepeatingDataSource(item, 1));
	}

	class RepeatingDataSource implements DataSource {
		private final DataItem<?> item;
		private int index;
		private final int size;

		private RepeatingDataSource(DataItem<?> item, int size) {
			this(item, 0, size);
		}

		private RepeatingDataSource(DataItem<?> item, int index, int size) {
			this.item = item;
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
				if (!item.equals(this.item))
					return false;

			return thatDataSource.index() == index && thatDataSource.size() == size;
		}

		@Override
		public int hashCode() {
			return item.hashCode() ^ index ^ size;
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
			return item;
		}

		@Override
		public <U extends DataTarget> U pipe(U target, int items) {
			if (items + index() >= size)
				throw new ArrayIndexOutOfBoundsException(size);

			if (items < 0)
				throw new ArrayIndexOutOfBoundsException(items);

			for (int i = 0; i < items; i++)
				target.put(item);

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
			return new RepeatingDataSource(item, index, size);
		}
	}
}
