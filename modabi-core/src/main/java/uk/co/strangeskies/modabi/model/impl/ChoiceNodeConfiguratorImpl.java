package uk.co.strangeskies.modabi.model.impl;

import uk.co.strangeskies.modabi.model.ChoiceNode;
import uk.co.strangeskies.modabi.model.building.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public class ChoiceNodeConfiguratorImpl extends
		BranchingNodeConfiguratorImpl<ChoiceNodeConfigurator, ChoiceNode> implements
		ChoiceNodeConfigurator, ChoiceNode {
	private String inMethod;
	private boolean inMethodChained;
	private boolean mandatory;

	public ChoiceNodeConfiguratorImpl(NodeBuilderContext context) {
		super(context);
	}

	@Override
	public ChoiceNode tryCreate() {
		return this;
	}

	@Override
	public Boolean isInMethodChained() {
		return inMethodChained;
	}

	@Override
	public String getInMethod() {
		return inMethod;
	}

	@Override
	public Boolean isMandatory() {
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

	@Override
	public void process(SchemaProcessingContext context) {
		context.accept(this);
	}
}
