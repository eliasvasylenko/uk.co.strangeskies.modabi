package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.modabi.model.nodes.BindingNode;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.UnbindingStrategy;

public interface BindingNodeConfigurator<S extends BindingNodeConfigurator<S, N, T>, N extends BindingNode<T>, T>
		extends SchemaNodeConfigurator<S, N> {
	public <V extends T> BindingNodeConfigurator<?, ?, V> dataClass(
			Class<V> dataClass);

	public S bindingStrategy(BindingStrategy strategy);

	public S unbindingStrategy(UnbindingStrategy strategy);

	public S bindingClass(Class<?> bindingClass);

	public S unbindingClass(Class<?> unbindingClass);

	public S unbindingMethod(String unbindingMethod);
}