package uk.co.strangeskies.modabi.schema.node.builder;

import uk.co.strangeskies.modabi.schema.node.SequenceNode;
import uk.co.strangeskies.modabi.schema.processing.DataInput;

public interface BranchNodeBuilder<U extends DataInput<? extends U>>
		extends
		BranchingNodeBuilder<BranchNodeBuilder<U>, SequenceNode<U>, U> {
	public BranchNodeBuilder<U> choice(boolean isChoice);
}
