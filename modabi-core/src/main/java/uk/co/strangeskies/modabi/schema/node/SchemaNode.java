package uk.co.strangeskies.modabi.schema.node;

import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface SchemaNode<T extends SchemaProcessingContext<? extends T>> {
	public void process(T context);
}
