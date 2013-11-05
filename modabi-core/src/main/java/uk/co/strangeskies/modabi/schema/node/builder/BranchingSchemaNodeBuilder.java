package uk.co.strangeskies.modabi.schema.node.builder;

import uk.co.strangeskies.modabi.schema.node.BranchingSchemaNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;

public interface BranchingSchemaNodeBuilder extends
		SchemaNodeBuilder<BranchingSchemaNode> {
	public BranchingSchemaNodeBuilder addChild(SchemaNode child);

	public BranchingSchemaNodeBuilder choice(boolean isChoice);

	public BranchingSchemaNodeBuilder inMethod(String inMethodName);
}
