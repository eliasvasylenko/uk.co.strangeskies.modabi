package uk.co.strangeskies.modabi.node;

import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public interface ChoiceNode<T extends SchemaProcessingContext<? extends T>>
		extends BranchingNode<T> {
	boolean isMandatory();
}
