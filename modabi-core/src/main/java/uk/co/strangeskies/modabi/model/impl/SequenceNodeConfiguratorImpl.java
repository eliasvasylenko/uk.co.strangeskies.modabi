package uk.co.strangeskies.modabi.model.impl;

import uk.co.strangeskies.modabi.model.SequenceNode;
import uk.co.strangeskies.modabi.model.build.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public class SequenceNodeConfiguratorImpl extends
		BranchingNodeConfiguratorImpl<SequenceNodeConfigurator, SequenceNode>
		implements SequenceNodeConfigurator, SequenceNode {
	public SequenceNodeConfiguratorImpl(NodeBuilderContext context) {
		super(context);
	}

	private String inMethod;
	private boolean inMethodChained;

	@Override
	public SequenceNode tryCreate() {
		return this;
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
	public void process(SchemaProcessingContext context) {
		context.accept(this);
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
