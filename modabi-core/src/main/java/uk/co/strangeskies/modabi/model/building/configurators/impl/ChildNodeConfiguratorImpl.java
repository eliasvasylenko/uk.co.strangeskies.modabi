package uk.co.strangeskies.modabi.model.building.configurators.impl;

import java.util.LinkedHashSet;

import uk.co.strangeskies.modabi.model.building.DataLoader;
import uk.co.strangeskies.modabi.model.building.configurators.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;

public abstract class ChildNodeConfiguratorImpl<S extends SchemaNodeConfigurator<S, N>, N extends ChildNode<?>, C extends ChildNode<?>, B extends BindingChildNode<?, ?>>
		extends SchemaNodeConfiguratorImpl<S, N, C, B> {
	private final SchemaNodeConfigurationContext<? super N> context;

	public ChildNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super N> parent) {
		this.context = parent;

		addResultListener(result -> parent.addChild(result));
	}

	protected SchemaNodeConfigurationContext<? super N> getContext() {
		return context;
	}

	@Override
	protected Class<?> getCurrentChildOutputTargetClass() {
		return getContext().getCurrentChildOutputTargetClass();
	}

	@Override
	protected LinkedHashSet<N> getOverriddenNodes() {
		return (getId() == null) ? new LinkedHashSet<>() : getContext()
				.overrideChild(getId(), getNodeClass());
	}

	@Override
	protected DataLoader getDataLoader() {
		return getContext().getDataLoader();
	}

	@Override
	protected boolean isAbstract() {
		return getContext().isAbstract();
	}
}
