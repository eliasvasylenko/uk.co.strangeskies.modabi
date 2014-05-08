package uk.co.strangeskies.modabi.model.nodes;

import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface ElementNode<T> extends AbstractModel<T>, BindingChildNode<T> {
	@Override
	default void process(SchemaProcessingContext context) {
		context.accept(this);
	}
}
