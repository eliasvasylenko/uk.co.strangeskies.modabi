package uk.co.strangeskies.modabi.schema.node.building.configuration;

import uk.co.strangeskies.modabi.schema.node.InputSequenceNode;

public interface InputSequenceNodeConfigurator extends
		ChildNodeConfigurator<InputSequenceNodeConfigurator, InputSequenceNode>,
		SchemaNodeConfigurator<InputSequenceNodeConfigurator, InputSequenceNode>,
		InputNodeConfigurator<InputSequenceNodeConfigurator, InputSequenceNode> {
}
