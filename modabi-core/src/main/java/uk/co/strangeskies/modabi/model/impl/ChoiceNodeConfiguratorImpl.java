package uk.co.strangeskies.modabi.model.impl;

import java.util.Collection;
import java.util.List;

import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChoiceNode;

public class ChoiceNodeConfiguratorImpl extends
		ChildNodeConfiguratorImpl<ChoiceNodeConfigurator, ChoiceNode> implements
		ChoiceNodeConfigurator {
	protected static class ChoiceNodeImpl extends SchemaNodeImpl implements
			ChildNodeImpl, ChoiceNode {
		private final boolean mandatory;

		public ChoiceNodeImpl(ChoiceNodeConfiguratorImpl configurator) {
			super(configurator);

			mandatory = configurator.mandatory;
		}

		public ChoiceNodeImpl(ChoiceNode node,
				Collection<? extends ChoiceNode> overriddenNodes,
				List<ChildNodeImpl> effectiveChildren) {
			super(node, overriddenNodes, effectiveChildren);

			mandatory = getValue(node, overriddenNodes, n -> n.isMandatory());
		}

		@Override
		public final Boolean isMandatory() {
			return mandatory;
		}

		@Override
		public void unbind(UnbindingChildContext context) {
			// TODO Auto-generated method stub

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
	public ChoiceNodeConfigurator mandatory(boolean mandatory) {
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
	protected Class<?> getCurrentChildOutputTargetClass() {
		return getContext().getCurrentChildOutputTargetClass();
	}

	@Override
	public ChildBuilder addChild() {
		return super.addChild();
	}
}
