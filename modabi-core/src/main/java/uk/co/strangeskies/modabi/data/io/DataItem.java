package uk.co.strangeskies.modabi.data.io;

public class DataItem<T> {
	private final DataType<T> type;
	private final T data;

	public DataItem(DataType<T> type, T data) {
		this.type = type;
		this.data = data;
	}

	public DataType<T> type() {
		return type;
	}

	public T data() {
		return data;
	}

	@SuppressWarnings("unchecked")
	public <U> U data(DataType<U> type) {
		if (this.type != type)
			throw new ClassCastException();
		return (U) data;
	}

	@Override
	public boolean equals(Object that) {
		if (!(that instanceof DataItem))
			return false;

		DataItem<?> thatDataItem = (DataItem<?>) that;

		return type == thatDataItem.type && data.equals(thatDataItem.data);
	}

	@Override
	public int hashCode() {
		return type.hashCode() + data.hashCode();
	}
}
