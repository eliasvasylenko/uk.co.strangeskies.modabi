package uk.co.strangeskies.modabi.data.io;

public interface DataSource {
	public int index();

	public <T> T get(DataType<T> type);

	public BufferedDataSource buffer(int items);

	public default BufferedDataSource bufferNext() {
		return buffer(1);
	}

	public <T extends DataTarget> T pipe(T target, int items);

	public default <T extends DataTarget> T pipeNext(T target) {
		return pipe(target, 1);
	}

	public static <T> DataSource repeating(DataType<T> type, T data) {
		return repeating(new DataItem<>(type, data));
	}

	public static <T> DataSource repeating(DataItem<T> item) {
		return new RepeatingDataSource(item);
	}

	class RepeatingDataSource implements DataSource {
		private final DataItem<?> item;
		private int index;

		protected RepeatingDataSource(DataItem<?> item) {
			this(item, 0);
		}

		protected RepeatingDataSource(DataItem<?> item, int index) {
			this.item = item;
			this.index = index;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof RepeatingDataSource))
				return false;

			RepeatingDataSource that = (RepeatingDataSource) obj;
			return !that.isTerminating() && item.equals(that.item)
					&& index == that.index;
		}

		@Override
		public int hashCode() {
			return item.hashCode() ^ index ^ (isTerminating() ? 1 : 0);
		}

		public boolean isTerminating() {
			return false;
		}

		protected DataItem<?> getItem() {
			return item;
		}

		@Override
		public int index() {
			return index;
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
			if (items < 0)
				throw new ArrayIndexOutOfBoundsException(items);

			for (int i = 0; i < items; i++)
				target.put(item);

			return target;
		}

		@Override
		public BufferedDataSource buffer(int items) {
			return pipe(new BufferingDataTarget(), items).buffer();
		}
	}
}
