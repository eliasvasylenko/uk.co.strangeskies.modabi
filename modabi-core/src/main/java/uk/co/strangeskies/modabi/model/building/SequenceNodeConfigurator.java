package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.modabi.model.SequenceNode;

public interface SequenceNodeConfigurator extends
		BranchingNodeConfigurator<SequenceNodeConfigurator, SequenceNode>,
		InputNodeConfigurator<SequenceNodeConfigurator, SequenceNode> {
}
