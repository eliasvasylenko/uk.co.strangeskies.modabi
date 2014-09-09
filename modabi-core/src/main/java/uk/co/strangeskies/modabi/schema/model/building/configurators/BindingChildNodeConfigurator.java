package uk.co.strangeskies.modabi.schema.model.building.configurators;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;

public interface BindingChildNodeConfigurator<S extends BindingChildNodeConfigurator<S, N, T, C, B>, N extends BindingChildNode<T, ?, ?>, T, C extends ChildNode<?, ?>, B extends BindingChildNode<?, ?, ?>>
		extends BindingNodeConfigurator<S, N, T, C, B>,
		InputNodeConfigurator<S, N>, ChildNodeConfigurator<S, N> {
	public S outMethod(String methodName);

	public S outMethodIterable(boolean iterable);

	public S occurances(Range<Integer> occuranceRange);

	public S extensible(boolean extensible);

	public S ordered(boolean ordered);

	/*
	 * TODO 'isOrdered' hint, for ranges above ..2, to help magically minimise
	 * impact of updating a 'ModifiableStructuredDataTarget' (e.g. saving over an
	 * existing XML document) by not considering it a violation of model equality
	 * to reorder from outMethod iterator.
	 */
}
