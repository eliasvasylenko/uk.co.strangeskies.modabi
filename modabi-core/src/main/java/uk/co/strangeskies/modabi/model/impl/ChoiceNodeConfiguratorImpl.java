package uk.co.strangeskies.modabi.model.impl;

import java.util.Collection;
import java.util.List;

import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChoiceNode;

public class ChoiceNodeConfiguratorImpl<C extends ChildNode, B extends BindingChildNode<?>>
		extends
		ChildNodeConfiguratorImpl<ChoiceNodeConfigurator<C, B>, ChoiceNode, C, B>
		implements ChoiceNodeConfigurator<C, B> {
	protected static class ChoiceNodeImpl extends SchemaNodeImpl implements
			ChildNodeImpl, ChoiceNode {
		private final boolean mandatory;

		public ChoiceNodeImpl(ChoiceNodeConfiguratorImpl<?, ?> configurator) {
			super(configurator);

			mandatory = configurator.mandatory;
		}

		public ChoiceNodeImpl(ChoiceNode node,
				Collection<? extends ChoiceNode> overriddenNodes,
				List<ChildNode> effectiveChildren) {
			super(node, overriddenNodes, effectiveChildren);

			mandatory = getValue(node, overriddenNodes, n -> n.isMandatory());
		}

		@Override
		public final Boolean isMandatory() {
			return mandatory;
		}
	}

	private boolean mandatory;

	public ChoiceNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super ChildNode> parent) {
		super(parent);
	}

	@Override
	public ChoiceNode tryCreate() {
		return new ChoiceNodeImpl(this);
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
	protected Class<?> getCurrentChildInputTargetClass() {
		return getContext().getCurrentChildInputTargetClass();
	}

	@Override
	protected ChoiceNode getEffective(ChoiceNode node) {
		return new ChoiceNodeImpl(node, getOverriddenNodes(),
				getEffectiveChildren());
	}

	@Override
	public ChildBuilder<C, B> addChild() {
		return childBuilder();
	}
}
