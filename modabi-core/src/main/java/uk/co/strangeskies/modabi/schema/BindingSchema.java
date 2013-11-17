package uk.co.strangeskies.modabi.schema;

import java.util.Set;

import uk.co.strangeskies.modabi.schema.node.ElementSchemaNode;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface BindingSchema<T, U extends SchemaProcessingContext<? extends U>> {
	public Set<ElementSchemaNode<?, ? super U>> getModelSet();

	public ElementSchemaNode<T, ? super U> getRoot();
}
