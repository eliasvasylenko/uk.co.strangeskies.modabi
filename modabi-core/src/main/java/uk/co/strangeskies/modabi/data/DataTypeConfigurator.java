package uk.co.strangeskies.modabi.data;

import java.util.function.Function;

import uk.co.strangeskies.gears.utilities.factory.Factory;
import uk.co.strangeskies.modabi.model.building.DataChildBuilder;
import uk.co.strangeskies.modabi.model.building.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.UnbindingStrategy;

public interface DataTypeConfigurator<T> extends Factory<DataType<T>> {
	/**
	 * @param name
	 *          The value to be returned by {@link DataType#getName()}.
	 * @return
	 */
	DataTypeConfigurator<T> name(String name);

	/**
	 * @param name
	 *          The value to be returned by {@link DataType#getBaseType()}.
	 * @return
	 */
	<U extends T> DataTypeConfigurator<U> dataClass(Class<U> dataClass);

	/**
	 * @param name
	 *          The value to be returned by {@link DataType#getBindingClass()}.
	 * @return
	 */
	DataTypeConfigurator<T> bindingClass(Class<?> builderClass);

	/**
	 * @param name
	 *          The value to be returned by {@link DataType#getBindingStrategy()}.
	 * @return
	 */
	DataTypeConfigurator<T> bindingStrategy(BindingStrategy strategy);

	/**
	 * @param name
	 *          The value to be returned by {@link DataType#getUnbindingClass()}.
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
	 *          The value to be returned by {@link DataType#getFactoryMethod()}.
	 * @return
	 */
	DataTypeConfigurator<T> unbindingMethod(String name);

	default DataTypeConfigurator<T> addChild(
			Function<DataChildBuilder, SchemaNodeConfigurator<?, ?>> propertyConfiguration) {
		propertyConfiguration.apply(addChild()).create();
		return this;
	}

	DataChildBuilder addChild();
}
