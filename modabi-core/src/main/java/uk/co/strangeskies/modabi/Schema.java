package uk.co.strangeskies.modabi;

import java.util.Set;

import uk.co.strangeskies.modabi.node.BindingNode;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public interface Schema<T, U extends SchemaProcessingContext<? extends U>> {
	public Set<BindingNode<?, ? super U>> getModelSet();

	public BindingNode<T, ? super U> getRoot();
}
