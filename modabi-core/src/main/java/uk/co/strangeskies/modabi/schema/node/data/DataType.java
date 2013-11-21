package uk.co.strangeskies.modabi.schema.node.data;

public interface DataType<T> {
	String getName();

	Class<T> getDataClass();

	String getParseMethod();

	Class<?> getFactoryClass();

	String getFactoryMethod();
}
