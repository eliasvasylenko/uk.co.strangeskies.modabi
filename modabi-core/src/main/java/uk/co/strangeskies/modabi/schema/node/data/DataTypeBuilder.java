package uk.co.strangeskies.modabi.schema.node.data;

import uk.co.strangeskies.gears.utilities.Factory;

public interface DataTypeBuilder<T> extends Factory<DataType<T>> {
	/**
	 * @param name
	 *          The value to be returned by {@link DataType#getName()}.
	 * @return
	 */
	DataTypeBuilder<T> name(String name);

	/**
	 * @param name
	 *          The value to be returned by {@link DataType#getDataClass()}.
	 * @return
	 */
	<U extends T> DataTypeBuilder<U> dataClass(Class<U> dataClass);

	/**
	 * @param name
	 *          The value to be returned by {@link DataType#getName()}.
	 * @return
	 */
	DataTypeBuilder<T> parseMethod(String name);

	/**
	 * @param name
	 *          The value to be returned by {@link DataType#getName()}.
	 * @return
	 */
	DataTypeBuilder<T> factoryClass(Class<?> factoryClass);

	/**
	 * @param name
	 *          The value to be returned by {@link DataType#getName()}.
	 * @return
	 */
	DataTypeBuilder<T> factoryMethod(String name);
}
