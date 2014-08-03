package uk.co.strangeskies.modabi.model.building.configurators.impl;

import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.configurators.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.ChildNodeImpl;
import uk.co.strangeskies.modabi.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.model.building.impl.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.SequenceNode;

public class SequenceNodeConfiguratorImpl<C extends ChildNode<?>, B extends BindingChildNode<?, ?>>
		extends
		ChildNodeConfiguratorImpl<SequenceNodeConfigurator<C, B>, SequenceNode, C, B>
		implements SequenceNodeConfigurator<C, B> {
	protected static class SequenceNodeImpl extends
			SchemaNodeImpl<SequenceNode.Effective> implements
			ChildNodeImpl<SequenceNode.Effective>, SequenceNode {
		private class Effective extends
				SchemaNodeImpl.Effective<SequenceNode.Effective> implements
				SequenceNode.Effective {
			public Effective(
					OverrideMerge<SequenceNode, SequenceNodeConfiguratorImpl<?, ?>> overrideMerge) {
				super(overrideMerge);
			}
		}

		private final Effective effective;

		public SequenceNodeImpl(SequenceNodeConfiguratorImpl<?, ?> configurator) {
			super(configurator);

			effective = new Effective(overrideMerge(this, configurator));
		}

		@Override
		public Effective effective() {
			return effective;
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
					.get(getChildren().getChildren().size() - 1).effective()
					.getPostInputClass();
	}

	@Override
	public ChildBuilder<C, B> addChild() {
		return super.addChild();
	}
}
