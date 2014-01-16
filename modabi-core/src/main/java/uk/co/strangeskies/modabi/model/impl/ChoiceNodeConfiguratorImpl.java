package uk.co.strangeskies.modabi.model.impl;

import java.util.List;

import uk.co.strangeskies.modabi.model.ChoiceNode;
import uk.co.strangeskies.modabi.model.SchemaNode;
import uk.co.strangeskies.modabi.model.building.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public class ChoiceNodeConfiguratorImpl extends
		BranchingNodeConfiguratorImpl<ChoiceNodeConfigurator, ChoiceNode> implements
		ChoiceNodeConfigurator, ChoiceNode {
	protected static class ChoiceNodeImpl extends BranchingNodeImpl implements
			ChoiceNode {
		private final String inMethod;
		private final boolean inMethodChained;
		private final boolean mandatory;

		public ChoiceNodeImpl(String id, List<SchemaNode> children,
				String inMethod, boolean inMethodChained, boolean mandatory) {
			super(id, children);
			this.inMethod = inMethod;
			this.inMethodChained = inMethodChained;
			this.mandatory = mandatory;
		}

		@Override
		public final void process(SchemaProcessingContext context) {
			context.accept(this);
		}

		@Override
		public final String getInMethod() {
			return inMethod;
		}

		@Override
		public final boolean isInMethodChained() {
			return inMethodChained;
		}

		@Override
		public final boolean isMandatory() {
			return mandatory;
		}
	}

	private String inMethod;
	private boolean inMethodChained;
	private boolean mandatory;

	public ChoiceNodeConfiguratorImpl(NodeBuilderContext context) {
		super(context);
	}

	@Override
	public ChoiceNode tryCreate() {
		return new ChoiceNodeImpl(getId(), getChildren(), inMethod,
				inMethodChained, mandatory);
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
	public boolean isMandatory() {
		return mandatory;
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
}
