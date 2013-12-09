package uk.co.strangeskies.modabi.node.impl;

import java.util.Collection;

import uk.co.strangeskies.modabi.node.SchemaNode;
import uk.co.strangeskies.modabi.node.SequenceNode;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public class SequenceNodeImpl extends BranchingNodeImpl implements SequenceNode {
	public SequenceNodeImpl(Collection<? extends SchemaNode> children,
			String inMethod, boolean inMethodChained) {
		super(children, inMethod, inMethodChained);
	}

	@Override
	public void process(SchemaProcessingContext context) {
		context.accept(this);
	}
}
