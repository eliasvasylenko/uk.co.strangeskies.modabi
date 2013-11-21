package uk.co.strangeskies.modabi.schema.node.impl;

import java.util.Collection;

import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.SequenceNode;
import uk.co.strangeskies.modabi.schema.processing.DataInput;

public class SequenceNodeImpl<T extends DataInput<T>> extends
		BranchingNodeImpl<T> implements SequenceNode<T> {
	public SequenceNodeImpl(Collection<? extends SchemaNode<? super T>> children,
			String inMethod, boolean inMethodChained) {
		super(children, inMethod, inMethodChained);
	}

	@Override
	public void process(T context) {
		context.branch(this);
	}
}
