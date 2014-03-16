package uk.co.strangeskies.modabi.model.nodes;

import java.util.List;

public interface BranchingNode extends SchemaNode {
	public List<SchemaNode> getChildren();
}
