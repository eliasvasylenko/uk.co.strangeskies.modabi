package uk.co.strangeskies.modabi.schema.node.building.configuration;

import uk.co.strangeskies.modabi.schema.node.ChoiceNode;

public interface ChoiceNodeConfigurator extends
		ChildNodeConfigurator<ChoiceNodeConfigurator, ChoiceNode>,
		SchemaNodeConfigurator<ChoiceNodeConfigurator, ChoiceNode> {
	public ChoiceNodeConfigurator mandatory(boolean mandatory);
}
