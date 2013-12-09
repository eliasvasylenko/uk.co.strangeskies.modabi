package uk.co.strangeskies.modabi.node;

import java.util.List;

public interface BranchingNode extends InputNode {
	public List<SchemaNode> getChildren();
}
