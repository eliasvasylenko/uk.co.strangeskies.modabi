package uk.co.strangeskies.modabi.schema.model.building.configurators;

import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SequenceNode;

public interface SequenceNodeConfigurator<C extends ChildNode<?, ?>, B extends BindingChildNode<?, ?, ?>>
		extends
		ChildNodeConfigurator<SequenceNodeConfigurator<C, B>, SequenceNode>,
		BranchingNodeConfigurator<SequenceNodeConfigurator<C, B>, SequenceNode, C, B> {
}
