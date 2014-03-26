package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;

public interface BindingChildNodeConfigurator<S extends BindingChildNodeConfigurator<S, N, T>, N extends BindingChildNode<T>, T>
		extends InputNodeConfigurator<S, N> {
	public S outMethod(String methodName);

	public S outMethodIterable(boolean iterable);

	public <V extends T> BindingChildNodeConfigurator<?, ?, V> dataClass(
			Class<V> dataClass);
}
