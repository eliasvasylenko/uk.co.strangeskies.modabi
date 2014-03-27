package uk.co.strangeskies.modabi.model.impl;

import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.modabi.model.building.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;

public abstract class ChildNodeConfiguratorImpl<S extends SchemaNodeConfigurator<S, N>, N extends ChildNode>
		extends SchemaNodeConfiguratorImpl<S, N> implements
		SchemaNodeConfigurator<S, N> {
	private final SchemaNodeConfiguratorImpl<?, ?> parent;

	public ChildNodeConfiguratorImpl(SchemaNodeConfiguratorImpl<?, ?> parent) {
		this.parent = parent;

		addResultListener(result -> parent.addChild(result, getEffective(result)));
	}

	protected SchemaNodeConfiguratorImpl<?, ?> getParent() {
		return parent;
	}

	@Override
	protected Class<?> getCurrentChildOutputTargetClass() {
		return getParent().getCurrentChildOutputTargetClass();
	}

	protected List<N> getOverriddenNodes() {
		return (getId() == null) ? new ArrayList<>() : getParent().overrideChild(
				getId(), getNodeClass());
	}

	@Override
	protected void finaliseProperties() {
		List<ChildNode> newInheritedChildren = new ArrayList<>();
		getOverriddenNodes().forEach(n -> {
			newInheritedChildren.addAll(n.getChildren());
		});
		inheritChildren(0, newInheritedChildren);
	}
}
