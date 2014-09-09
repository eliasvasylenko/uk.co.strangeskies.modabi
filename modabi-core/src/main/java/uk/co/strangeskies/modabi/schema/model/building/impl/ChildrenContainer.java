package uk.co.strangeskies.modabi.schema.model.building.impl;

import java.util.List;

import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;

public class ChildrenContainer {
	private final List<ChildNode<?, ?>> children;
	private final List<ChildNode.Effective<?, ?>> effectiveChildren;

	public ChildrenContainer(List<ChildNode<?, ?>> children,
			List<ChildNode.Effective<?, ?>> effectiveChildren) {
		this.children = children;
		this.effectiveChildren = effectiveChildren;
	}

	public List<ChildNode<?, ?>> getChildren() {
		return children;
	}

	public List<ChildNode.Effective<?, ?>> getEffectiveChildren() {
		return effectiveChildren;
	}
}
