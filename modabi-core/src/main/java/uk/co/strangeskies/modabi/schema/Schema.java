package uk.co.strangeskies.modabi.schema;

import java.util.Set;

import uk.co.strangeskies.modabi.schema.node.BindingNode;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface Schema<T, U extends SchemaProcessingContext<? extends U>> {
	public Set<BindingNode<?, ? super U>> getModelSet();

	public BindingNode<T, ? super U> getRoot();
}
