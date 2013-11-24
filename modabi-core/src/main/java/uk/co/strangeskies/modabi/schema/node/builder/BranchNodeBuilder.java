package uk.co.strangeskies.modabi.schema.node.builder;

import uk.co.strangeskies.modabi.schema.node.SequenceNode;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface BranchNodeBuilder<U extends SchemaProcessingContext<? extends U>>
		extends
		BranchingNodeBuilder<BranchNodeBuilder<U>, SequenceNode<U>, U> {
	public BranchNodeBuilder<U> choice(boolean isChoice);
}
