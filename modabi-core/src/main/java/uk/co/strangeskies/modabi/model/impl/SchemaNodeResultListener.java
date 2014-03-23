package uk.co.strangeskies.modabi.model.impl;

import uk.co.strangeskies.modabi.model.nodes.SchemaNode;

public interface SchemaNodeResultListener<T extends SchemaNode> {
	void addChild(T result, T effective);
}
