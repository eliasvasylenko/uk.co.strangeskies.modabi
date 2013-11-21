package uk.co.strangeskies.modabi.schema.node;

import uk.co.strangeskies.modabi.schema.processing.DataInput;

public interface ChoiceNode<T extends DataInput<? extends T>>
		extends BranchingNode<T> {
	boolean isMandatory();
}
