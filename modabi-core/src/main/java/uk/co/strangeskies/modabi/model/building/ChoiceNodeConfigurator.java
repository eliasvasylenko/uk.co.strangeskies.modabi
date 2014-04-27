package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChoiceNode;

public interface ChoiceNodeConfigurator
		extends
		BranchingNodeConfigurator<ChoiceNodeConfigurator, ChoiceNode, ChildBuilder, ChildNode> {
	public ChoiceNodeConfigurator mandatory(boolean mandatory);
}
