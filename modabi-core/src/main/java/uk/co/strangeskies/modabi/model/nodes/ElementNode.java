package uk.co.strangeskies.modabi.model.nodes;

import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface ElementNode<T> extends
		AbstractModel<T, ElementNode.Effective<T>>,
		BindingChildNode<T, ElementNode.Effective<T>> {
	interface Effective<T> extends ElementNode<T>,
			BindingChildNode.Effective<T, Effective<T>> {
		@Override
		default void process(SchemaProcessingContext context) {
			context.accept(this);
		}
	}
}
