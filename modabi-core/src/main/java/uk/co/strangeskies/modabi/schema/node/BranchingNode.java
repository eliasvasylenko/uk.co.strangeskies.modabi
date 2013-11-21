package uk.co.strangeskies.modabi.schema.node;

import java.util.List;

import uk.co.strangeskies.modabi.schema.processing.DataInput;

public interface BranchingNode<T extends DataInput<? extends T>>
		extends InputNode<T> {
	public List<SchemaNode<? super T>> getChildren();
}
