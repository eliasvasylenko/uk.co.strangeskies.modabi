package uk.co.strangeskies.modabi.model.impl;

import uk.co.strangeskies.modabi.model.nodes.ChildNode;

public interface SchemaNodeResultListener<T extends ChildNode> {
	void addChild(T result, T effective);
}
