package uk.co.strangeskies.modabi.model.building.configurators.impl;

import java.util.Collection;
import java.util.List;

import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.configurators.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.ChildNodeImpl;
import uk.co.strangeskies.modabi.model.building.impl.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.SequenceNode;

public class SequenceNodeConfiguratorImpl<C extends ChildNode, B extends BindingChildNode<?>>
		extends
		ChildNodeConfiguratorImpl<SequenceNodeConfigurator<C, B>, SequenceNode, C, B>
		implements SequenceNodeConfigurator<C, B> {
	protected static class SequenceNodeImpl extends SchemaNodeImpl implements
			ChildNodeImpl, SequenceNode {
		public SequenceNodeImpl(SequenceNodeConfiguratorImpl<?, ?> configurator) {
			super(configurator);
		}

		public SequenceNodeImpl(SequenceNode node,
				Collection<? extends SequenceNode> overriddenNodes,
				List<ChildNode> effectiveChildren) {
			super(node, overriddenNodes, effectiveChildren);
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof SequenceNode))
				return false;
			return super.equals(obj);
		}
	}

	public SequenceNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super SequenceNode> parent) {
		super(parent);
	}

	@Override
	public SequenceNode tryCreate() {
		return new SequenceNodeImpl(this);
	}

	@Override
	protected Class<SequenceNode> getNodeClass() {
		return SequenceNode.class;
	}

	@Override
	protected Class<?> getCurrentChildInputTargetClass() {
		if (getChildren().getChildren().isEmpty())
			return getContext().getCurrentChildInputTargetClass();
		else
			return getChildren().getChildren()
					.get(getChildren().getChildren().size() - 1).getPostInputClass();
	}

	@Override
	protected SequenceNode getEffective(SequenceNode node) {
		return new SequenceNodeImpl(node, getOverriddenNodes(), getChildren()
				.getEffectiveChildren());
	}

	@Override
	public ChildBuilder<C, B> addChild() {
		return childBuilder();
	}
}
