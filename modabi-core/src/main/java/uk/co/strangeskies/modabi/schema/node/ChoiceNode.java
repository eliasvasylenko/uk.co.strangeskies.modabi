package uk.co.strangeskies.modabi.schema.node;

import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface ChoiceNode<T extends SchemaProcessingContext<? extends T>>
		extends BranchingNode<T> {
	boolean isMandatory();
}
