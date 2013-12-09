package uk.co.strangeskies.modabi.node.builder;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.node.BindingNode;

public interface BindingNodeBuilder<T> extends
		BranchingNodeBuilder<BindingNodeBuilder<T>, BindingNode<T>> {
	public BindingNodeBuilder<T> name(String name);

	public <V extends T> BindingNodeBuilder<V> base(BindingNode<? super V> base);

	public BindingNodeBuilder<T> occurances(Range<Integer> occuranceRange);

	public <V extends T> BindingNodeBuilder<V> dataClass(Class<V> dataClass);

	public BindingNodeBuilder<T> factoryClass(Class<?> factoryClass);

	public BindingNodeBuilder<T> outMethod(String outMethodName);

	public BindingNodeBuilder<T> iterable(boolean isIterable);

	public BindingNodeBuilder<T> factoryMethod(String buildMethodName);
}
