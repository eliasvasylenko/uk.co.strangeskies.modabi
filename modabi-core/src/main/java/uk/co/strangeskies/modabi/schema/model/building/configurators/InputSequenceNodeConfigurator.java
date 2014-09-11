package uk.co.strangeskies.modabi.schema.model.building.configurators;

import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.InputSequenceNode;

public interface InputSequenceNodeConfigurator<C extends BindingChildNode<?, ?, ?>>
		extends
		ChildNodeConfigurator<InputSequenceNodeConfigurator<C>, InputSequenceNode, C, C>,
		SchemaNodeConfigurator<InputSequenceNodeConfigurator<C>, InputSequenceNode, C, C>,
		InputNodeConfigurator<InputSequenceNodeConfigurator<C>, InputSequenceNode, C, C> {
}
