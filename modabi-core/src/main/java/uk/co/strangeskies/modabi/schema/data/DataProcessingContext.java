package uk.co.strangeskies.modabi.schema.data;

public interface DataProcessingContext {
	void processChild(String name);

	void processProperty(String name);

	void processData();

	void processData(String data);

	void processData(byte data);

	void processData(short data);

	void processData(int data);

	void processData(long data);

	void processData(float data);

	void processData(double data);

	void processEnd();
}
