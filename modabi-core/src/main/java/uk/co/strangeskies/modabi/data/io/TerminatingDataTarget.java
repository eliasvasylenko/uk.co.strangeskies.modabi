package uk.co.strangeskies.modabi.data.io;

public interface TerminatingDataTarget extends DataTarget {
	@Override
	public default <T> TerminatingDataTarget put(DataType<T> type, T data) {
		DataTarget.super.put(type, data);
		return this;
	}

	@Override
	public <T> TerminatingDataTarget put(DataItem<T> item);

	public void terminate();

	public boolean isTerminated();
}
