package uk.co.strangeskies.modabi.schema.node;

import java.util.List;

import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface BranchingSchemaNode<T extends SchemaProcessingContext<? extends T>>
		extends SchemaNode<T> {
	public String getInMethod();

	public List<SchemaNode<? super T>> getChildren();
}
