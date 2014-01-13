package uk.co.strangeskies.modabi.model;

import java.util.List;

public interface BranchingNode extends SchemaNode {
	public List<SchemaNode> getChildren();
}
