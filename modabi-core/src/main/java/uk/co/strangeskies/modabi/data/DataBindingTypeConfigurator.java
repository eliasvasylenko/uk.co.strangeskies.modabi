package uk.co.strangeskies.modabi.data;

import java.util.function.Function;

import uk.co.strangeskies.gears.utilities.factory.Factory;
import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.configurators.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.DataNodeChildNode;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;

public interface DataBindingTypeConfigurator<T> extends
		Factory<DataBindingType<T>> {
	/**
	 * @param name
	 *          The value to be returned by {@link DataBindingType#getName()}.
	 * @return
	 */
	DataBindingTypeConfigurator<T> name(String name);

	/**
	 * @param name
	 *          The value to be returned by {@link DataBindingType#getName()}.
	 * @return
	 */
	DataBindingTypeConfigurator<T> isAbstract(boolean hidden);

	/**
	 * @param name
	 *          The value to be returned by {@link DataBindingType#getName()}.
	 * @return
	 */
	DataBindingTypeConfigurator<T> isPrivate(boolean hidden);

	/**
	 * @param name
	 *          The value to be returned by {@link DataBindingType#getBaseType()}.
	 * @return
	 */
	<U extends T> DataBindingTypeConfigurator<U> dataClass(Class<U> dataClass);

	<U extends T> DataBindingTypeConfigurator<U> baseType(
			DataBindingType<? super U> baseType);

	/**
	 * @param name
	 *          The value to be returned by
	 *          {@link DataBindingType#getBindingClass()}.
	 * @return
	 */
	DataBindingTypeConfigurator<T> bindingClass(Class<?> builderClass);

	/**
	 * @param name
	 *          The value to be returned by
	 *          {@link DataBindingType#getBindingStrategy()}.
	 * @return
	 */
	DataBindingTypeConfigurator<T> bindingStrategy(BindingStrategy strategy);

	/**
	 * @param name
	 *          The value to be returned by
	 *          {@link DataBindingType#getUnbindingClass()}.
	 * @return
	 */
	DataBindingTypeConfigurator<T> unbindingClass(Class<?> builderClass);

	/**
	 * @param name
	 *          The value to be returned by
	 *          {@link DataBindingType#getUnbindingClass()}.
	 * @return
	 */
	DataBindingTypeConfigurator<T> unbindingFactroyClass(Class<?> factoryClass);

	/**
	 * @param name
	 *          The value to be returned by
	 *          {@link DataBindingType#getUnbindingStrategy()}.
	 * @return
	 */
	DataBindingTypeConfigurator<T> unbindingStrategy(UnbindingStrategy strategy);

	/**
	 * @param name
	 *          The value to be returned by
	 *          {@link DataBindingType#getFactoryMethod()}.
	 * @return
	 */
	DataBindingTypeConfigurator<T> unbindingMethod(String name);

	default DataBindingTypeConfigurator<T> addChild(
			Function<ChildBuilder<DataNodeChildNode, DataNode<?>>, SchemaNodeConfigurator<?, ? extends DataNodeChildNode>> propertyConfiguration) {
		propertyConfiguration.apply(addChild()).create();
		return this;
	}

	ChildBuilder<DataNodeChildNode, DataNode<?>> addChild();
}
