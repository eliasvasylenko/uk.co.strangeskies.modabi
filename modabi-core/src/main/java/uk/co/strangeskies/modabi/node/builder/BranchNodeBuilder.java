package uk.co.strangeskies.modabi.node.builder;

import uk.co.strangeskies.modabi.node.SequenceNode;

public interface BranchNodeBuilder extends
		BranchingNodeBuilder<BranchNodeBuilder, SequenceNode> {
	public BranchNodeBuilder choice(boolean isChoice);
}
