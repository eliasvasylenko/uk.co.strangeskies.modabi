package uk.co.strangeskies.modabi.model.building.configurators;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;

public interface BindingChildNodeConfigurator<S extends BindingChildNodeConfigurator<S, N, T>, N extends BindingChildNode<T>, T>
		extends BindingNodeConfigurator<S, N, T>, InputNodeConfigurator<S, N> {
	public S outMethod(String methodName);

	public S outMethodIterable(boolean iterable);

	public S occurances(Range<Integer> occuranceRange);
}
