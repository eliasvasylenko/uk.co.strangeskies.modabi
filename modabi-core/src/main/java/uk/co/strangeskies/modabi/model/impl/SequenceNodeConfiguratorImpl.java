package uk.co.strangeskies.modabi.model.impl;

import java.util.List;

import uk.co.strangeskies.modabi.model.SchemaNode;
import uk.co.strangeskies.modabi.model.SequenceNode;
import uk.co.strangeskies.modabi.model.building.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public class SequenceNodeConfiguratorImpl extends
		BranchingNodeConfiguratorImpl<SequenceNodeConfigurator, SequenceNode>
		implements SequenceNodeConfigurator, SequenceNode {
	protected static class SequenceNodeImpl extends BranchingNodeImpl implements
			SequenceNode {
		private final String inMethod;
		private final boolean inMethodChained;

		public SequenceNodeImpl(String id, List<SchemaNode> children,
				String inMethod, boolean inMethodChained) {
			super(id, children);
			this.inMethod = inMethod;
			this.inMethodChained = inMethodChained;
		}

		@Override
		public void process(SchemaProcessingContext context) {
			context.accept(this);
		}

		@Override
		public String getInMethod() {
			return inMethod;
		}

		@Override
		public boolean isInMethodChained() {
			return inMethodChained;
		}
	}

	public SequenceNodeConfiguratorImpl(NodeBuilderContext context) {
		super(context);
	}

	private String inMethod;
	private boolean inMethodChained;

	@Override
	public SequenceNode tryCreate() {
		return new SequenceNodeImpl(getId(), getChildren(), inMethod,
				inMethodChained);
	}

	@Override
	public boolean isInMethodChained() {
		return inMethodChained;
	}

	@Override
	public String getInMethod() {
		return inMethod;
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
}
