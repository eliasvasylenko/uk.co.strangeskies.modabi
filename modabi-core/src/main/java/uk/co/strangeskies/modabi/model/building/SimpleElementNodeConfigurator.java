package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.model.nodes.SimpleElementNode;

public interface SimpleElementNodeConfigurator<T>
		extends
		TypedDataNodeConfigurator<SimpleElementNodeConfigurator<T>, SimpleElementNode<T>, T>,
		RepeatableNodeConfigurator<SimpleElementNodeConfigurator<T>, SimpleElementNode<T>> {
	@Override
	public <U extends T> SimpleElementNodeConfigurator<U> type(DataType<U> type);

	@Override
	public <V extends T> SimpleElementNodeConfigurator<V> dataClass(
			Class<V> dataClass);
}
