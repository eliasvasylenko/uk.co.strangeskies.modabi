package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.model.PropertyNode;

public interface PropertyNodeConfigurator<T> extends
		TypedDataNodeConfigurator<PropertyNodeConfigurator<T>, PropertyNode<T>, T>,
		OptionalNodeConfigurator<PropertyNodeConfigurator<T>, PropertyNode<T>> {
	@Override
	public <U extends T> PropertyNodeConfigurator<U> type(DataType<U> type);

	@Override
	public <V extends T> PropertyNodeConfigurator<V> dataClass(Class<V> dataClass);
}
