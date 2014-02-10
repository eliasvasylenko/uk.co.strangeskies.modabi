package uk.co.strangeskies.modabi.model.impl;

import uk.co.strangeskies.modabi.model.SequenceNode;
import uk.co.strangeskies.modabi.model.building.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

class SequenceNodeConfiguratorImpl extends
		BranchingNodeConfiguratorImpl<SequenceNodeConfigurator, SequenceNode>
		implements SequenceNodeConfigurator {
	protected static class SequenceNodeImpl extends
			BranchingNodeImpl<SequenceNode> implements SequenceNode {
		private final String inMethod;
		private final boolean inMethodChained;

		public SequenceNodeImpl(SequenceNodeConfiguratorImpl configurator) {
			super(configurator);

			inMethod = configurator.inMethod;
			inMethodChained = configurator.inMethodChained;
		}

		public SequenceNodeImpl(SequenceNodeImpl node, SequenceNode overriddenNode) {
			super(node, overriddenNode);
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
		public void process(SchemaProcessingContext context) {
			context.accept(this);
		}

		@Override
		protected void validateEffectiveModel() {
		}
	}

	public SequenceNodeConfiguratorImpl(BranchingNodeConfiguratorImpl<?, ?> parent) {
		super(parent);
	}

	private String inMethod;
	private boolean inMethodChained;

	@Override
	public SequenceNode tryCreate() {
		return new SequenceNodeImpl(this);
	}

	@Override
	public SequenceNodeConfigurator inMethod(String methodName) {
		inMethod = methodName;

		return this;
	}

	@Override
	public SequenceNodeConfigurator inMethodChained(boolean chained) {
		inMethodChained = chained;

		return this;
	}

	@Override
	public Class<SequenceNode> getNodeClass() {
		return SequenceNode.class;
	}
}
