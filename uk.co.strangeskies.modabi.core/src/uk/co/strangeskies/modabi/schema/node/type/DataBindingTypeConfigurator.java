package uk.co.strangeskies.modabi.schema.node.type;

import java.util.function.Function;

import uk.co.strangeskies.modabi.schema.TypeLiteral;
import uk.co.strangeskies.modabi.schema.node.building.ChildBuilder;
import uk.co.strangeskies.modabi.schema.node.building.configuration.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.SchemaNodeConfigurator;

public interface DataBindingTypeConfigurator<T>
		extends
		BindingNodeConfigurator<DataBindingTypeConfigurator<T>, DataBindingType<T>, T> {
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
	@SuppressWarnings("unchecked")
	@Override
	default <U extends T> DataBindingTypeConfigurator<U> dataClass(
			Class<U> dataClass) {
		return (DataBindingTypeConfigurator<U>) BindingNodeConfigurator.super
				.dataClass(dataClass);
	}

	@Override
	<U extends T> DataBindingTypeConfigurator<U> dataType(TypeLiteral<U> dataClass);

	<U extends T> DataBindingTypeConfigurator<U> baseType(
			DataBindingType<? super U> baseType);

	@Override
	default public DataBindingTypeConfigurator<T> addChild(
			Function<ChildBuilder, SchemaNodeConfigurator<?, ?>> propertyConfiguration) {
		propertyConfiguration.apply(addChild()).create();
		return this;
	}

	@Override
	ChildBuilder addChild();
}
