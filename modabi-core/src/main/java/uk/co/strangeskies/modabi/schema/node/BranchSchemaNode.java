package uk.co.strangeskies.modabi.schema.node;

import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface BranchSchemaNode<T extends SchemaProcessingContext<? extends T>>
		extends BranchingSchemaNode<T> {
	public boolean isChoice();
}
