package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.model.TypedDataNode;

public interface TypedDataNodeConfigurator<S extends TypedDataNodeConfigurator<S, N, T>, N extends TypedDataNode<T>, T>
		extends DataNodeConfigurator<S, N, T> {
	public <U extends T> TypedDataNodeConfigurator<?, ?, U> type(DataType<U> type);

	@Override
	public <V extends T> TypedDataNodeConfigurator<?, ?, V> dataClass(
			Class<V> dataClass);

	public S value(T data);
}
