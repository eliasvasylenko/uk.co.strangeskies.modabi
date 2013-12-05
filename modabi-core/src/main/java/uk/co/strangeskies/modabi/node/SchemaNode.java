package uk.co.strangeskies.modabi.node;

import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public interface SchemaNode<T extends SchemaProcessingContext<? extends T>> {
	public void process(T context);
}
