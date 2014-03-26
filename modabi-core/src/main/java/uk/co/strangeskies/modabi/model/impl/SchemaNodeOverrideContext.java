package uk.co.strangeskies.modabi.model.impl;

import java.util.List;

import uk.co.strangeskies.modabi.model.nodes.ChildNode;

public interface SchemaNodeOverrideContext<T extends ChildNode> {
	List<T> overrideChild(String id, Class<T> nodeClass);
}
