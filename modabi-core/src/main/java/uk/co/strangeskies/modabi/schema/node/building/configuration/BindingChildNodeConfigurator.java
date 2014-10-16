package uk.co.strangeskies.modabi.schema.node.building.configuration;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.schema.node.BindingChildNode;

public interface BindingChildNodeConfigurator<S extends BindingChildNodeConfigurator<S, N, T>, N extends BindingChildNode<T, ?, ?>, T>
		extends BindingNodeConfigurator<S, N, T>, InputNodeConfigurator<S, N>,
		ChildNodeConfigurator<S, N> {
	public S outMethod(String methodName);

	public S outMethodIterable(boolean iterable);

	public S occurrences(Range<Integer> occuranceRange);

	public S extensible(boolean extensible);

	public S ordered(boolean ordered);

	/*
	 * TODO 'isOrdered' hint, for ranges above ..2, to help magically minimise
	 * impact of updating a 'ModifiableStructuredDataTarget' (e.g. saving over an
	 * existing XML document) by not considering it a violation of model equality
	 * to reorder from outMethod iterator.
	 */
}
