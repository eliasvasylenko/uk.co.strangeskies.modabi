package uk.co.strangeskies.modabi.schema.node.builder;

import uk.co.strangeskies.modabi.schema.node.BranchSchemaNode;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface BranchSchemaNodeBuilder<U extends SchemaProcessingContext<? extends U>>
		extends
		BranchingSchemaNodeBuilder<BranchSchemaNodeBuilder<U>, BranchSchemaNode<U>, U> {
	public BranchSchemaNodeBuilder<U> choice(boolean isChoice);
}
