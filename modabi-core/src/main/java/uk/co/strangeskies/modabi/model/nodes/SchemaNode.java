package uk.co.strangeskies.modabi.model.nodes;

import java.util.List;

public interface SchemaNode {
	String getId();

	List<? extends ChildNode> getChildren();
}
