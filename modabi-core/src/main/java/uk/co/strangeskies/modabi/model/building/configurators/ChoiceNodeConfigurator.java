package uk.co.strangeskies.modabi.model.building.configurators;

import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChoiceNode;

public interface ChoiceNodeConfigurator<C extends ChildNode<?, ?>, B extends BindingChildNode<?, ?, ?>>
		extends ChildNodeConfigurator<ChoiceNodeConfigurator<C, B>, ChoiceNode>,
		BranchingNodeConfigurator<ChoiceNodeConfigurator<C, B>, ChoiceNode, C, B> {
	public ChoiceNodeConfigurator<C, B> mandatory(boolean mandatory);
}
