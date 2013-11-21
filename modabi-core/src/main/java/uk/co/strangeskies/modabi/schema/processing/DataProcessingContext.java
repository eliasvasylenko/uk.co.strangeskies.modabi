package uk.co.strangeskies.modabi.schema.processing;

public interface DataProcessingContext {
	void processChild(String name);

	void processProperty(String name, String value);

	void processData(String data);

	void processEnd();
}
