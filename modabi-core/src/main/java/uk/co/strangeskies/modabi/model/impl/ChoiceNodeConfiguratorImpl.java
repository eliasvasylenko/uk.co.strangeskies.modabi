package uk.co.strangeskies.modabi.model.impl;

import java.util.List;

import uk.co.strangeskies.modabi.model.ChoiceNode;
import uk.co.strangeskies.modabi.model.building.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

class ChoiceNodeConfiguratorImpl extends
		BranchingNodeConfiguratorImpl<ChoiceNodeConfigurator, ChoiceNode> implements
		ChoiceNodeConfigurator {
	protected static class ChoiceNodeImpl extends BranchingNodeImpl implements
			ChoiceNode {
		private final boolean mandatory;

		public ChoiceNodeImpl(ChoiceNodeConfiguratorImpl configurator) {
			super(configurator);

			mandatory = configurator.mandatory;
		}

		public ChoiceNodeImpl(ChoiceNodeImpl node, List<ChoiceNode> overriddenNodes) {
			super(node, overriddenNodes);

			mandatory = getValue(node, overriddenNodes, n -> n.isMandatory());
		}

		@Override
		public final Boolean isMandatory() {
			return mandatory;
		}

		@Override
		public <U> U process(SchemaProcessingContext<U> context) {
			return context.accept(this);
		}

		@Override
		protected void validateAsEffectiveModel(boolean isAbstract) {
			super.validateAsEffectiveModel(isAbstract);
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
	public Class<ChoiceNode> getNodeClass() {
		return ChoiceNode.class;
	}
}
