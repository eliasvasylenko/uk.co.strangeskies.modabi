package uk.co.strangeskies.modabi.data;

public interface DataType<T> {
	String getName();

	DataType<T> getBaseType();

	String getParseMethod();

	Class<?> getFactoryClass();

	String getFactoryMethod();
}
