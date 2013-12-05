package uk.co.strangeskies.modabi.data;

public interface DataType<T> {
	String getName();

	Class<T> getDataClass();

	String getParseMethod();

	Class<?> getFactoryClass();

	String getFactoryMethod();
}
