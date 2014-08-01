package uk.co.strangeskies.modabi.model.building.configurators;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;

public interface BindingChildNodeConfigurator<S extends BindingChildNodeConfigurator<S, N, T, C, B>, N extends BindingChildNode<T, ?>, T, C extends ChildNode<?>, B extends BindingChildNode<?, ?>>
		extends BindingNodeConfigurator<S, N, T, C, B>, InputNodeConfigurator<S, N> {
	public S outMethod(String methodName);

	public S outMethodIterable(boolean iterable);

	public S occurances(Range<Integer> occuranceRange);
}
