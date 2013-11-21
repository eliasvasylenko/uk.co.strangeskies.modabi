package uk.co.strangeskies.modabi.schema.node.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.co.strangeskies.modabi.schema.node.BranchingNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.processing.DataInput;

public abstract class BranchingNodeImpl<T extends DataInput<? extends T>>
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
