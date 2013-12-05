package uk.co.strangeskies.modabi.node.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.co.strangeskies.modabi.node.BranchingNode;
import uk.co.strangeskies.modabi.node.SchemaNode;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public abstract class BranchingNodeImpl<T extends SchemaProcessingContext<? extends T>>
		implements BranchingNode<T> {
	private final List<SchemaNode<? super T>> children;

	private final String inMethod;
	private final boolean inMethodChained;

	public BranchingNodeImpl(
			Collection<? extends SchemaNode<? super T>> children, String inMethod,
			boolean inMethodChained) {
		this.children = new ArrayList<>(children);
		this.inMethod = inMethod;
		this.inMethodChained = inMethodChained;
	}

	@Override
	public List<SchemaNode<? super T>> getChildren() {
		return children;
	}

	@Override
	public String getInMethod() {
		return inMethod;
	}

	@Override
	public boolean isInMethodChained() {
		return inMethodChained;
	}
}
