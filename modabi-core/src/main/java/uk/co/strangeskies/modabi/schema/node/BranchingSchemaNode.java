package uk.co.strangeskies.modabi.schema.node;

import java.util.List;

public interface BranchingSchemaNode extends SchemaNode {
	public List<SchemaNode> getChildren();

	public boolean isChoice();

	public String getInMethod();
}
