package uk.co.strangeskies.modabi.schema.node.data;

import uk.co.strangeskies.gears.utilities.Factory;

public interface SchemaDataTypeBuilder<T> extends Factory<SchemaDataType<T>> {
	SchemaDataTypeBuilder<T> name(String name);

	SchemaDataTypeBuilder<T> parseMethod(String name);

	<U extends T> SchemaDataTypeBuilder<U> dataClass(Class<U> dataClass);

	SchemaDataTypeBuilder<T> factoryClass(Class<?> factoryClass);

	SchemaDataTypeBuilder<T> factoryMethod(String name);
}
