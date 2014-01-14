package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.model.ContentNode;

public interface ContentNodeConfigurator<T> extends
		TypedDataNodeConfigurator<ContentNodeConfigurator<T>, ContentNode<T>, T>,
		OptionalNodeConfigurator<ContentNodeConfigurator<T>, ContentNode<T>> {
	@Override
	public <V extends T> ContentNodeConfigurator<V> dataClass(Class<V> dataClass);

	@Override
	public <U extends T> ContentNodeConfigurator<U> type(DataType<U> type);
}
