package uk.co.strangeskies.modabi.data;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.configurators.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.DataNodeChildNode;

public interface DataBindingTypeConfigurator<T>
		extends
		BindingNodeConfigurator<DataBindingTypeConfigurator<T>, DataBindingType<T>, T> {
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
	@Override
	<U extends T> DataBindingTypeConfigurator<U> dataClass(Class<U> dataClass);

	<U extends T> DataBindingTypeConfigurator<U> baseType(
			DataBindingType<? super U> baseType);

	default DataBindingTypeConfigurator<T> addChild(
			Function<ChildBuilder<DataNodeChildNode<?>, DataNode<?>>, SchemaNodeConfigurator<?, ? extends DataNodeChildNode<?>>> propertyConfiguration) {
		propertyConfiguration.apply(addChild()).create();
		return this;
	}

	ChildBuilder<DataNodeChildNode<?>, DataNode<?>> addChild();

	DataBindingTypeConfigurator<T> providedUnbindingParameters(
			List<String> parameterNames);

	default DataBindingTypeConfigurator<T> providedUnbindingParameters(
			String... parameterNames) {
		return providedUnbindingParameters(Arrays.asList(parameterNames));
	}
}
