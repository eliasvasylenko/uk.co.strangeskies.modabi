package uk.co.strangeskies.modabi.data;

public interface DataProcessingContext {
	void processChild(String name);

	void processProperty(String name);

	void processData();

	void processData(String data);

	<T> void processData(T data);

	void processEnd();
}
