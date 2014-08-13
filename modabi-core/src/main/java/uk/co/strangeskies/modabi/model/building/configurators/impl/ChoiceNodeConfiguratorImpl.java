package uk.co.strangeskies.modabi.model.building.configurators.impl;

import java.util.Objects;

import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.configurators.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.ChildNodeImpl;
import uk.co.strangeskies.modabi.model.building.impl.ChildrenConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.model.building.impl.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChoiceNode;

public class ChoiceNodeConfiguratorImpl<C extends ChildNode<?, ?>, B extends BindingChildNode<?, ?, ?>>
		extends
		ChildNodeConfiguratorImpl<ChoiceNodeConfigurator<C, B>, ChoiceNode, C, B>
		implements ChoiceNodeConfigurator<C, B> {
	protected static class ChoiceNodeImpl extends
			SchemaNodeImpl<ChoiceNode, ChoiceNode.Effective> implements
			ChildNodeImpl<ChoiceNode, ChoiceNode.Effective>, ChoiceNode {
		private static class Effective extends
				SchemaNodeImpl.Effective<ChoiceNode, ChoiceNode.Effective> implements
				ChoiceNode.Effective {
			private final boolean mandatory;

			public Effective(
					OverrideMerge<ChoiceNode, ChoiceNodeConfiguratorImpl<?, ?>> overrideMerge) {
				super(overrideMerge);

				mandatory = overrideMerge.getValue(ChoiceNode::isMandatory);
			}

			@Override
			public Boolean isMandatory() {
				return mandatory;
			}
		}

		private final Effective effective;

		private final boolean mandatory;

		public ChoiceNodeImpl(ChoiceNodeConfiguratorImpl<?, ?> configurator) {
			super(configurator);

			mandatory = configurator.mandatory;

			effective = new Effective(overrideMerge(this, configurator));
		}

		@Override
		public final Boolean isMandatory() {
			return mandatory;
		}

		@Override
		public Effective effective() {
			return effective;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ChoiceNode))
			return false;

		ChoiceNode other = (ChoiceNode) obj;
		return super.equals(obj) && Objects.equals(mandatory, other.isMandatory());
	}

	private boolean mandatory;

	public ChoiceNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super ChildNode<?, ?>> parent) {
		super(parent);
	}

	@Override
	public ChoiceNode tryCreate() {
		return new ChoiceNodeImpl(this);
	}

	@Override
	public ChildrenConfigurator<C, B> createChildrenConfigurator() {
		return null; // TODO create hiding children configurator! options can be
									// reduced, not increased, but overriding nodes.
	}

	@Override
	public ChoiceNodeConfigurator<C, B> mandatory(boolean mandatory) {
		this.mandatory = mandatory;

		return this;
	}

	@Override
	protected Class<ChoiceNode> getNodeClass() {
		return ChoiceNode.class;
	}

	@Override
	public ChildBuilder<C, B> addChild() {
		return super.addChild();
	}
}
