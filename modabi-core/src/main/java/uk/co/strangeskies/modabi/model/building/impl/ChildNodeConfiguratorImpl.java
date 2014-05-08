package uk.co.strangeskies.modabi.model.building.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.co.strangeskies.modabi.model.building.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;

public abstract class ChildNodeConfiguratorImpl<S extends SchemaNodeConfigurator<S, N>, N extends ChildNode, C extends ChildNode, B extends BindingChildNode<?>>
		extends SchemaNodeConfiguratorImpl<S, N, C, B> {
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

	protected Set<N> getOverriddenNodes() {
		return (getId() == null) ? new HashSet<>() : getContext().overrideChild(
				getId(), getNodeClass());
	}

	@Override
	protected void finaliseProperties() {
		if (!isFinalisedProperties()) {
			List<ChildNodeImpl> newInheritedChildren = new ArrayList<>();
			getOverriddenNodes().forEach(
					c -> c.getChildren().forEach(
							n -> newInheritedChildren.add((ChildNodeImpl) n)));

			inheritChildren(0, newInheritedChildren);
		}

		super.finaliseProperties();
	}
}
