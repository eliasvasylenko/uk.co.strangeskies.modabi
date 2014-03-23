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
	 *          {@link DataType#getImplementationStrategy()}.
	 * @return
	 */
	DataTypeConfigurator<T> bindingStrategy(BindingStrategy strategy);

	DataTypeConfigurator<T> unbindingStrategy(UnbindingStrategy strategy);

	/**
	 * @param name
	 *          The value to be returned by {@link DataType#getBaseType()}.
	 * @return
	 */
	<U extends T> DataTypeConfigurator<U> dataClass(Class<U> dataClass);

	/**
	 * @param name
	 *          The value to be returned by {@link DataType#getBuilderClass()}.
	 * @return
	 */
	DataTypeConfigurator<T> builderClass(Class<?> builderClass);

	/**
	 * @param name
	 *          The value to be returned by {@link DataType#getFactoryMethod()}.
	 * @return
	 */
	DataTypeConfigurator<T> buildMethod(String name);
}
