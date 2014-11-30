package uk.co.strangeskies.modabi.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BufferingDataTarget extends DataTargetDecorator {
	public BufferingDataTarget() {
		super(new BufferingDataTargetImpl());
	}

	public DataSource buffer() {
		return ((BufferingDataTargetImpl) getComponent()).buffer();
	}

	@Override
	public <T> BufferingDataTarget put(DataType<T> type, T data) {
		super.put(type, data);
		return this;
	}

	@Override
	public <U> BufferingDataTarget put(DataItem<U> item) {
		super.put(item);
		return this;
	}
}

class BufferingDataTargetImpl implements DataTarget {
	private List<DataItem<?>> dataSequence = new ArrayList<>();
	private boolean terminated;

	@Override
	public <T> DataTarget put(DataItem<T> item) {
		dataSequence.add(item);
		return this;
	}

	@Override
	public void terminate() {
		if (!terminated)
			dataSequence = Collections.unmodifiableList(dataSequence);
		terminated = true;
	}

	@Override
	public DataStreamState currentState() {
		return null;
	}

	public BufferedDataSource buffer() {
		terminate();
		return new BufferedDataSource(dataSequence);
	}

	private static class BufferedDataSource implements DataSource {
		private final List<DataItem<?>> dataSequence;
		private int index;

		public BufferedDataSource(List<DataItem<?>> dataSequence) {
			this(dataSequence, 0);
		}

		public BufferedDataSource(List<DataItem<?>> dataSequence, int index) {
			this.dataSequence = dataSequence;
			this.index = index;
		}

		@Override
		public boolean equals(Object that) {
			if (!(that instanceof DataSource))
				return false;

			DataSource thatDataSource = (DataSource) that;

			return thatDataSource.index() == index()
					&& thatDataSource.size() == size()
					&& dataSequence.equals(thatDataSource.copy().reset()
							.pipe(new BufferingDataTargetImpl()).buffer().dataSequence);
		}

		@Override
		public int hashCode() {
			return index ^ dataSequence.hashCode();
		}

		@Override
		public DataItem<?> get() {
			return dataSequence.get(index++);
		}

		@Override
		public int size() {
			return dataSequence.size();
		}

		@Override
		public DataSource reset() {
			index = 0;
			return this;
		}

		@Override
		public <T extends DataTarget> T pipe(T target, int items) {
			for (int start = index; start < items; start++)
				target.put(dataSequence.get(start));

			return target;
		}

		@Override
		public int index() {
			return index;
		}

		@Override
		public DataStreamState currentState() {
			return null;
		}

		@Override
		public DataSource copy() {
			return new BufferedDataSource(dataSequence, index);
		}
	}
}