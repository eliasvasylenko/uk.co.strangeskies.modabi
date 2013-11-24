package uk.co.strangeskies.modabi.schema.node;

import java.util.List;

import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface BranchingNode<T extends SchemaProcessingContext<? extends T>>
		extends InputNode<T> {
	public List<SchemaNode<? super T>> getChildren();
}
