package uk.co.strangeskies.modabi.data.io;

import uk.co.strangeskies.utilities.Copyable;

public interface DataSource extends Copyable<DataSource> {
	DataStreamState currentState();

	DataSource reset();

	int index();

	<T> T get(DataType<T> type);

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
		public boolean equals(Object obj) {
			if (!(obj instanceof DataSource))
				return false;

			DataSource that = (DataSource) obj;
			return !that.copy().reset() && item.equals(that.item)
					&& index == that.index && size == that.size;
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

		protected DataItem<?> getItem() {
			return item;
		}

		protected void setIndex(int index) {
			this.index = index;
		}

		protected void incrementIndex() {
			index++;
		}

		protected RuntimeException unimplemented() {
			return new ClassCastException();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <U> U get(DataType<U> type) {
			if (type == item.type())
				return (U) item.data();
			throw new ClassCastException();
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
