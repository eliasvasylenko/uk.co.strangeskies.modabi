package uk.co.strangeskies.modabi.model.building.configurators;

import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.SequenceNode;

public interface SequenceNodeConfigurator<C extends ChildNode<?, ?>, B extends BindingChildNode<?, ?, ?>>
		extends
		ChildNodeConfigurator<SequenceNodeConfigurator<C, B>, SequenceNode>,
		BranchingNodeConfigurator<SequenceNodeConfigurator<C, B>, SequenceNode, C, B> {
}
