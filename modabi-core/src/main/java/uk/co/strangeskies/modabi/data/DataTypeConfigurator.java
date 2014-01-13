package uk.co.strangeskies.modabi.data;

import uk.co.strangeskies.gears.utilities.factory.Factory;

public interface DataTypeConfigurator<T> extends Factory<DataType<T>> {
	/**
	 * @param name
	 *          The value to be returned by {@link DataType#getName()}.
	 * @return
	 */
	DataTypeConfigurator<T> name(String name);

	/**
	 * @param name
	 *          The value to be returned by {@link DataType#getDataClass()}.
	 * @return
	 */
	<U extends T> DataTypeConfigurator<U> dataClass(Class<U> dataClass);

	/**
	 * @param name
	 *          The value to be returned by {@link DataType#getName()}.
	 * @return
	 */
	DataTypeConfigurator<T> parseMethod(String name);

	/**
	 * @param name
	 *          The value to be returned by {@link DataType#getName()}.
	 * @return
	 */
	DataTypeConfigurator<T> factoryClass(Class<?> factoryClass);

	/**
	 * @param name
	 *          The value to be returned by {@link DataType#getName()}.
	 * @return
	 */
	DataTypeConfigurator<T> factoryMethod(String name);
}
