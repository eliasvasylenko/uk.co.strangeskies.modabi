package uk.co.strangeskies.modabi.model.impl;

import java.util.Collection;

import uk.co.strangeskies.modabi.model.ChoiceNode;
import uk.co.strangeskies.modabi.model.building.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

class ChoiceNodeConfiguratorImpl extends
		BranchingNodeConfiguratorImpl<ChoiceNodeConfigurator, ChoiceNode> implements
		ChoiceNodeConfigurator {
	protected static class ChoiceNodeImpl extends BranchingNodeImpl implements
			ChoiceNode {
		private final String inMethod;
		private final boolean inMethodChained;
		private final boolean mandatory;

		public ChoiceNodeImpl(ChoiceNodeConfiguratorImpl configurator) {
			super(configurator);

			inMethod = configurator.inMethod;
			inMethodChained = configurator.inMethodChained;
			mandatory = configurator.mandatory;
		}

		public ChoiceNodeImpl(ChoiceNodeImpl node,
				Collection<ChoiceNode> overriddenNodes) {
			super(node, overriddenNodes);

			inMethod = getValue(node, overriddenNodes, n -> n.getInMethod());

			inMethodChained = getValue(node, overriddenNodes,
					n -> n.isInMethodChained());

			mandatory = getValue(node, overriddenNodes, n -> n.isMandatory());
		}

		@Override
		public final String getInMethod() {
			return inMethod;
		}

		@Override
		public final Boolean isInMethodChained() {
			return inMethodChained;
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
		protected void validateAsEffectiveModel(boolean isAbstract) {
			super.validateAsEffectiveModel(isAbstract);
		}
	}

	private String inMethod;
	private boolean inMethodChained;
	private boolean mandatory;

	public ChoiceNodeConfiguratorImpl(BranchingNodeConfiguratorImpl<?, ?> parent) {
		super(parent);
	}

	@Override
	public ChoiceNode tryCreate() {
		return new ChoiceNodeImpl(this);
	}

	@Override
	public ChoiceNodeConfigurator inMethod(String methodName) {
		inMethod = methodName;

		return this;
	}

	@Override
	public ChoiceNodeConfigurator inMethodChained(boolean chained) {
		inMethodChained = chained;

		return this;
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
