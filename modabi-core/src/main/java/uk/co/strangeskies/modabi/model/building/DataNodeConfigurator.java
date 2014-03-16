package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.modabi.model.nodes.DataNode;

public interface DataNodeConfigurator<S extends DataNodeConfigurator<S, N, T>, N extends DataNode<T>, T>
		extends InputNodeConfigurator<S, N> {
	public S outMethod(String methodName);

	public S outMethodIterable(boolean iterable);

	public <V extends T> DataNodeConfigurator<?, ?, V> dataClass(
			Class<V> dataClass);
}
