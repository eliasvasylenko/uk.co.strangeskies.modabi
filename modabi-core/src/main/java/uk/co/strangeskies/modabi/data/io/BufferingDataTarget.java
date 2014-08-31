package uk.co.strangeskies.modabi.data.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BufferingDataTarget extends TerminatingDataTargetDecorator {
	public BufferingDataTarget() {
		super(new BufferingDataTargetImpl());
	}

	public BufferedDataSource buffer() {
		return ((BufferingDataTargetImpl) getComponent()).buffer();
	}

	@Override
	public <T> BufferingDataTarget put(DataItem<T> item) {
		super.put(item);
		return this;
	}

	@Override
	public <T> BufferingDataTarget put(DataType<T> type, T data) {
		put(new DataItem<T>(type, data));
		return this;
	}
}

class BufferingDataTargetImpl implements TerminatingDataTarget {
	private List<DataItem<?>> dataSequence = new ArrayList<>();
	private boolean terminated;

	@Override
	public <T> TerminatingDataTarget put(DataItem<T> item) {
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
	public State currentState() {
		return null;
	}

	public BufferedDataSourceImpl buffer() {
		terminate();
		return new BufferedDataSourceImpl(dataSequence);
	}
}

class BufferedDataSourceImpl implements BufferedDataSource {
	private final List<DataItem<?>> dataSequence;
	private int index;

	public BufferedDataSourceImpl(List<DataItem<?>> dataSequence) {
		index = 0;
		this.dataSequence = dataSequence;
	}

	@Override
	public boolean equals(Object that) {
		if (!(that instanceof BufferedDataSource))
			return false;

		BufferedDataSource thatBufferedDataSource = (BufferedDataSource) that;

		return thatBufferedDataSource.index() != index
				&& dataSequence.equals(thatBufferedDataSource.copy().reset()
						.pipe(new BufferingDataTargetImpl()).buffer().dataSequence);
	}

	@Override
	public int hashCode() {
		return index ^ dataSequence.hashCode();
	}

	@Override
	public <T> T get(DataType<T> type) {
		return dataSequence.get(index++).data(type);
	}

	@Override
	public int size() {
		return dataSequence.size();
	}

	@Override
	public BufferedDataSource reset() {
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
	public BufferedDataSource buffer() {
		return buffer(dataSequence.size() - index);
	}

	@Override
	public BufferedDataSource buffer(int items) {
		return new BufferedDataSourceImpl(
				dataSequence.subList(index, index + items));
	}

	@Override
	public BufferedDataSource copy() {
		BufferedDataSourceImpl copy = new BufferedDataSourceImpl(dataSequence);
		copy.index = index;
		return copy;
	}

	@Override
	public int index() {
		return index;
	}
}