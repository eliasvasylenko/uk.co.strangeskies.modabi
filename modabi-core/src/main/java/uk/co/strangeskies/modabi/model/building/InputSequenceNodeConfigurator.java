package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.InputSequenceNode;

public interface InputSequenceNodeConfigurator
		extends
		BranchingNodeConfigurator<InputSequenceNodeConfigurator, InputSequenceNode, ChildBuilder, BindingChildNode<?>>,
		InputNodeConfigurator<InputSequenceNodeConfigurator, InputSequenceNode> {
}
