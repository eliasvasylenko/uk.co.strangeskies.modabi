package uk.co.strangeskies.modabi.node;

import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public interface SequenceNode<T extends SchemaProcessingContext<? extends T>>
		extends BranchingNode<T> {
}
