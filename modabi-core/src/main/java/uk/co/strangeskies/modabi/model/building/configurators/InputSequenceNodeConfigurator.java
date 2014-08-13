package uk.co.strangeskies.modabi.model.building.configurators;

import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.InputSequenceNode;

public interface InputSequenceNodeConfigurator<C extends BindingChildNode<?, ?, ?>>
		extends
		BranchingNodeConfigurator<InputSequenceNodeConfigurator<C>, InputSequenceNode, C, C>,
		InputNodeConfigurator<InputSequenceNodeConfigurator<C>, InputSequenceNode> {
}
