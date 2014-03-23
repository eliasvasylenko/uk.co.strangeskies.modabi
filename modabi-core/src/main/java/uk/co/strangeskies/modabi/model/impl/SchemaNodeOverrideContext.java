package uk.co.strangeskies.modabi.model.impl;

import java.util.List;

import uk.co.strangeskies.modabi.model.nodes.SchemaNode;

public interface SchemaNodeOverrideContext<T extends SchemaNode> {
	List<T> overrideChild(String id, Class<T> nodeClass);
}
