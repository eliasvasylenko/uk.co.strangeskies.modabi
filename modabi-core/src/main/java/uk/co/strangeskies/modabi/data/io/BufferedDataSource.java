package uk.co.strangeskies.modabi.data.io;

import uk.co.strangeskies.gears.utilities.Copyable;

public interface BufferedDataSource extends TerminatingDataSource,
		Copyable<BufferedDataSource> {
	BufferedDataSource reset();

	public static <T> BufferedDataSource repeating(DataType<T> type, T data,
			int times) {
		return repeating(new DataItem<>(type, data), times);
	}

	public static <T> BufferedDataSource repeating(DataItem<?> item, int times) {
		return new BufferedRepeatingDataSource(item, times);
	}

	public static <T> BufferedDataSource single(DataType<T> type, T data) {
		return single(new DataItem<>(type, data));
	}

	public static BufferedDataSource single(DataItem<?> item) {
		return new BufferedRepeatingDataSource(item, 1);
	}

	class BufferedRepeatingDataSource extends RepeatingDataSource implements
			BufferedDataSource {
		private final int size;

		public BufferedRepeatingDataSource(DataItem<?> item, int size) {
			this(item, 0, size);
		}

		public BufferedRepeatingDataSource(DataItem<?> item, int index, int size) {
			super(item, index);
			this.size = size;
		}

		@Override
		public BufferedDataSource buffer() {
			return new BufferedRepeatingDataSource(getItem(), 0, size - index());
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public BufferedDataSource copy() {
			return new BufferedRepeatingDataSource(getItem(), index(), size);
		}

		@Override
		public BufferedDataSource reset() {
			super.setIndex(0);
			return this;
		}

		@Override
		public <U extends DataTarget> U pipe(U target, int items) {
			if (items + index() >= size)
				throw new ArrayIndexOutOfBoundsException(size);

			return super.pipe(target, items);
		}
	}
}
