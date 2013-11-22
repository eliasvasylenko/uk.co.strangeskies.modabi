package uk.co.strangeskies.modabi.schema.processing;

public interface SchemaProcessingContext {
	<T> T fetchImplementation(Class<T> clazz);
}
