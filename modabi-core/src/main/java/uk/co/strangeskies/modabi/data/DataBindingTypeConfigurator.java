package uk.co.strangeskies.modabi.data;

import java.util.function.Function;

import uk.co.strangeskies.modabi.schema.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.schema.model.building.configurators.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNodeChildNode;

public interface DataBindingTypeConfigurator<T>
		extends
		BindingNodeConfigurator<DataBindingTypeConfigurator<T>, DataBindingType<T>, T, DataNodeChildNode<?, ?>, DataNode<?>> {
	/**
	 * @param name
	 *          The value to be returned by {@link DataBindingType#getName()}.
	 * @return
	 */
	@Override
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
	@Override
	<U extends T> DataBindingTypeConfigurator<U> dataClass(Class<U> dataClass);

	<U extends T> DataBindingTypeConfigurator<U> baseType(
			DataBindingType<? super U> baseType);

	@Override
	default public DataBindingTypeConfigurator<T> addChild(
			Function<ChildBuilder<DataNodeChildNode<?, ?>, DataNode<?>>, SchemaNodeConfigurator<?, ? extends DataNodeChildNode<?, ?>, ?, ?>> propertyConfiguration) {
		propertyConfiguration.apply(addChild()).create();
		return this;
	}

	@Override
	ChildBuilder<DataNodeChildNode<?, ?>, DataNode<?>> addChild();
}
