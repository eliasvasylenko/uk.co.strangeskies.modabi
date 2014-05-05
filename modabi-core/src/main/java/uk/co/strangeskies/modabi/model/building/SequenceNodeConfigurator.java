package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.SequenceNode;

public interface SequenceNodeConfigurator<C extends ChildNode, B extends BindingChildNode<?>>
		extends
		BranchingNodeConfigurator<SequenceNodeConfigurator<C, B>, SequenceNode, C, B> {
}
