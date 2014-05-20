package uk.co.strangeskies.modabi.data.io;

public interface DataTarget {
	public default <T> DataTarget put(DataType<T> type, T data) {
		return put(new DataItem<>(type, data));
	}

	public <T> DataTarget put(DataItem<T> item);
}
