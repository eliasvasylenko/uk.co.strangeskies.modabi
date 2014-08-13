package uk.co.strangeskies.modabi.model.building.impl;

import uk.co.strangeskies.modabi.model.nodes.ChildNode;

public interface ChildNodeImpl<S extends ChildNode<S, E>, E extends ChildNode.Effective<S, E>>
		extends ChildNode<S, E> {
}
