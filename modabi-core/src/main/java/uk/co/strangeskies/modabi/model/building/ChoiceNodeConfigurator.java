package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.modabi.model.ChoiceNode;

public interface ChoiceNodeConfigurator extends
		BranchingNodeConfigurator<ChoiceNodeConfigurator, ChoiceNode> {
	public ChoiceNodeConfigurator mandatory(boolean mandatory);
}
