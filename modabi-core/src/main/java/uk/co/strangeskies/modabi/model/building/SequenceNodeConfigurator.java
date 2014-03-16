package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.modabi.model.nodes.SequenceNode;

public interface SequenceNodeConfigurator extends
		BranchingNodeConfigurator<SequenceNodeConfigurator, SequenceNode>,
		InputNodeConfigurator<SequenceNodeConfigurator, SequenceNode> {
}
