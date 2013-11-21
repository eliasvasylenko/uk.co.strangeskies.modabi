package uk.co.strangeskies.modabi.schema.node;

import uk.co.strangeskies.modabi.schema.processing.DataInput;

public interface SequenceNode<T extends DataInput<? extends T>>
		extends BranchingNode<T> {
}
