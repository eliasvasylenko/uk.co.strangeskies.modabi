package uk.co.strangeskies.modabi.data;

import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.UnbindingStrategy;

public interface DataTypeConfigurator<T> extends
		DataTypeRestrictionConfigurator<T> {
	/**
	 * @param name
	 *          The value to be returned by {@link DataType#getName()}.
	 * @return
	 */
	DataTypeConfigurator<T> name(String name);

	/**
	 * @param name
	 *          The value to be returned by
	 *          {@link DataType#getBindingClass()}.
	 * @return
	 */
	DataTypeConfigurator<T> bindingClass(Class<?> builderClass);

	/**
	 * @param name
	 *          The value to be returned by
	 *          {@link DataType#getBindingStrategy()}.
	 * @return
	 */
	DataTypeConfigurator<T> bindingStrategy(BindingStrategy strategy);

	/**
	 * @param name
	 *          The value to be returned by
	 *          {@link DataType#getUnbindingClass()}.
	 * @return
	 */
	DataTypeConfigurator<T> unbindingClass(Class<?> builderClass);

	/**
	 * @param name
	 *          The value to be returned by
	 *          {@link DataType#getUnbindingStrategy()}.
	 * @return
	 */
	DataTypeConfigurator<T> unbindingStrategy(UnbindingStrategy strategy);

	/**
	 * @param name
	 *          The value to be returned by
	 *          {@link DataType#getBaseType()}.
	 * @return
	 */
	<U extends T> DataTypeConfigurator<U> dataClass(Class<U> dataClass);

	/**
	 * @param name
	 *          The value to be returned by
	 *          {@link DataType#getFactoryMethod()}.
	 * @return
	 */
	DataTypeConfigurator<T> buildMethod(String name);
}
