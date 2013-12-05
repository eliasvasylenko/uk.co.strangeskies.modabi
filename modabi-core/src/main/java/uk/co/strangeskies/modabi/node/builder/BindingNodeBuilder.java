package uk.co.strangeskies.modabi.node.builder;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.node.BindingNode;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public interface BindingNodeBuilder<T, U extends SchemaProcessingContext<? extends U>>
		extends
		BranchingNodeBuilder<BindingNodeBuilder<T, U>, BindingNode<T, U>, U> {
	public BindingNodeBuilder<T, U> name(String name);

	public <V extends T> BindingNodeBuilder<V, U> base(
			BindingNode<? super V, U> base);

	public BindingNodeBuilder<T, U> occurances(Range<Integer> occuranceRange);

	public <V extends T> BindingNodeBuilder<V, U> dataClass(Class<V> dataClass);

	public BindingNodeBuilder<T, U> factoryClass(Class<?> factoryClass);

	public BindingNodeBuilder<T, U> outMethod(String outMethodName);

	public BindingNodeBuilder<T, U> iterable(boolean isIterable);

	public BindingNodeBuilder<T, U> factoryMethod(String buildMethodName);
}
