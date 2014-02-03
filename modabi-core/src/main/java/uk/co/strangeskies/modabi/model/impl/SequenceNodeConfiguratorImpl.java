package uk.co.strangeskies.modabi.model.impl;

import uk.co.strangeskies.modabi.model.SequenceNode;
import uk.co.strangeskies.modabi.model.building.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

class SequenceNodeConfiguratorImpl extends
		BranchingNodeConfiguratorImpl<SequenceNodeConfigurator, SequenceNode>
		implements SequenceNodeConfigurator {
	protected static class SequenceNodeImpl extends EffectiveSequenceNodeImpl {
		private final EffectiveSequenceNodeImpl effectiveModel;

		SequenceNodeImpl(SequenceNodeConfiguratorImpl configurator) {
			super(configurator);

			SequenceNode overriddenNode = configurator.getOverriddenNode();
			effectiveModel = overriddenNode == null ? this
					: new EffectiveSequenceNodeImpl(this, overriddenNode);
		}

		@Override
		public EffectiveSequenceNodeImpl effectiveModel() {
			return effectiveModel;
		}
	}

	protected static class EffectiveSequenceNodeImpl extends BranchingNodeImpl
			implements SequenceNode {
		private final String inMethod;
		private final boolean inMethodChained;

		public EffectiveSequenceNodeImpl(SequenceNodeConfiguratorImpl configurator) {
			super(configurator);

			inMethod = configurator.inMethod;
			inMethodChained = configurator.inMethodChained;
		}

		public EffectiveSequenceNodeImpl(SequenceNodeImpl node,
				SequenceNode overriddenNode) {
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
		protected SchemaNodeImpl effectiveModel() {
			return this;
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
