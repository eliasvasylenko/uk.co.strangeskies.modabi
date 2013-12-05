package uk.co.strangeskies.modabi.node;

import java.util.List;

import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public interface BranchingNode<T extends SchemaProcessingContext<? extends T>>
		extends InputNode<T> {
	public List<SchemaNode<? super T>> getChildren();
}
