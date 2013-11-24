package uk.co.strangeskies.modabi.schema.data;

public interface DataType<T> {
	String getName();

	Class<T> getDataClass();

	String getParseMethod();

	Class<?> getFactoryClass();

	String getFactoryMethod();
}
