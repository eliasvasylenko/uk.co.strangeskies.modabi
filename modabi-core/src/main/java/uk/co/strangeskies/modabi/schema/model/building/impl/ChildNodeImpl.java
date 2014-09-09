package uk.co.strangeskies.modabi.schema.model.building.impl;

import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;

public interface ChildNodeImpl<S extends ChildNode<S, E>, E extends ChildNode.Effective<S, E>>
		extends ChildNode<S, E> {
}
