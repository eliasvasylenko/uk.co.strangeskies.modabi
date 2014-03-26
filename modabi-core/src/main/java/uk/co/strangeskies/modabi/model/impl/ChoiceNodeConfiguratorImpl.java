package uk.co.strangeskies.modabi.model.impl;

import java.util.Collection;
import java.util.List;

import uk.co.strangeskies.modabi.model.building.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChoiceNode;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;
import uk.co.strangeskies.modabi.processing.SchemaResultProcessingContext;

public class ChoiceNodeConfiguratorImpl extends
		BranchingNodeConfiguratorImpl<ChoiceNodeConfigurator, ChoiceNode> implements
		ChoiceNodeConfigurator {
	protected static class ChoiceNodeImpl extends BranchingNodeImpl implements
			ChoiceNode {
		private final boolean mandatory;

		public ChoiceNodeImpl(ChoiceNodeConfiguratorImpl configurator) {
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

		@Override
		public void process(SchemaProcessingContext context) {
			context.accept(this);
		}

		@Override
		public <U> U process(SchemaResultProcessingContext<U> context) {
			return context.accept(this);
		}
	}

	private boolean mandatory;

	public ChoiceNodeConfiguratorImpl(BranchingNodeConfiguratorImpl<?, ?> parent) {
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
	protected Class<?> getCurrentChildPreInputClass() {
		return getPreInputClass();
	}

	@Override
	protected ChoiceNode getEffective(ChoiceNode node) {
		return new ChoiceNodeImpl(node, getOverriddenNodes(),
				getEffectiveChildren());
	}
}
