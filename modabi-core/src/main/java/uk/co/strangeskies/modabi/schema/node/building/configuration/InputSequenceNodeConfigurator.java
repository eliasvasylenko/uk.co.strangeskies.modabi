package uk.co.strangeskies.modabi.schema.node.building.configuration;

import uk.co.strangeskies.modabi.schema.node.BindingChildNode;
import uk.co.strangeskies.modabi.schema.node.InputSequenceNode;

public interface InputSequenceNodeConfigurator<C extends BindingChildNode<?, ?, ?>>
		extends
		ChildNodeConfigurator<InputSequenceNodeConfigurator<C>, InputSequenceNode, C, C>,
		SchemaNodeConfigurator<InputSequenceNodeConfigurator<C>, InputSequenceNode, C, C>,
		InputNodeConfigurator<InputSequenceNodeConfigurator<C>, InputSequenceNode, C, C> {
}
