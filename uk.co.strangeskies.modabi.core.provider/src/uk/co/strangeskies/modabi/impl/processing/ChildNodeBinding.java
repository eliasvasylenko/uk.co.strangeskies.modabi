package uk.co.strangeskies.modabi.impl.processing;

import uk.co.strangeskies.modabi.schema.ChildNode;

public abstract class ChildNodeBinding<T extends ChildNode.Effective<?, ?>> {
	private BindingContextImpl context;
	private final T node;

	public ChildNodeBinding(BindingContextImpl context, T node) {
		this.context = context;
		this.node = node;
	}

	protected void setContext(BindingContextImpl context) {
		this.context = context;
	}

	public BindingContextImpl getContext() {
		return context;
	}

	public T getNode() {
		return node;
	}

	protected void repeatNode() {
		/*
		 * TODO generalise protected do while for multiple node occurrences, as seen
		 * in ComplexNodeBinding
		 */
	}
}
