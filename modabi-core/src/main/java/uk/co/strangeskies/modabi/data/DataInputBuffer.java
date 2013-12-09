package uk.co.strangeskies.modabi.data;

public interface DataInputBuffer {
	String nextChild();

	<T> T getProperty(String name, DataType<T> dataType);

	<T> T getData(DataType<T> dataType);

	void endChild();
}
