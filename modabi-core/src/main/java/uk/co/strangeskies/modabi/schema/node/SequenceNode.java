package uk.co.strangeskies.modabi.schema.node;

import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface SequenceNode<T extends SchemaProcessingContext<? extends T>>
		extends BranchingNode<T> {
}
