package uk.co.strangeskies.modabi.model.impl;

import uk.co.strangeskies.modabi.model.nodes.ChildNode;

public interface ChildNodeImpl extends ChildNode {
	void unbind(UnbindingChildContext context);
}
