package uk.co.strangeskies.modabi.model.impl;

import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.modabi.model.building.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;

public abstract class ChildNodeConfiguratorImpl<S extends SchemaNodeConfigurator<S, N>, N extends ChildNode>
		extends SchemaNodeConfiguratorImpl<S, N> implements
		SchemaNodeConfigurator<S, N> {
	private final SchemaNodeConfigurationContext<? super N> context;

	public ChildNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super N> parent) {
		this.context = parent;

		addResultListener(result -> parent.addChild(result, getEffective(result)));
	}

	protected SchemaNodeConfigurationContext<? super N> getContext() {
		return context;
	}

	@Override
	protected Class<?> getCurrentChildOutputTargetClass() {
		return getContext().getCurrentChildOutputTargetClass();
	}

	protected List<N> getOverriddenNodes() {
		return (getId() == null) ? new ArrayList<>() : getContext().overrideChild(
				getId(), getNodeClass());
	}

	@Override
	protected void finaliseProperties() {
		if (!isFinalisedProperties()) {
			List<ChildNode> newInheritedChildren = new ArrayList<>();
			getOverriddenNodes().forEach(n -> {
				newInheritedChildren.addAll(n.getChildren());
			});
			inheritChildren(0, newInheritedChildren);
		}

		super.finaliseProperties();
	}
}
