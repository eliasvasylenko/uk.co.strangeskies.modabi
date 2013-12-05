package uk.co.strangeskies.modabi.schema.data;

public interface DataProcessingContext<E extends > {
	void processChild(String name);

	void processProperty(String name);

	void processData();

	void processData(String data);

	<T> void processData(T data);

	void processEnd();
}
