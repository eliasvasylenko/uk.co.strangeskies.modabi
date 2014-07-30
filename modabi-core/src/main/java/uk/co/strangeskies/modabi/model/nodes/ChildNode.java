package uk.co.strangeskies.modabi.model.nodes;

import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface ChildNode<E extends ChildNode.Effective<E>> extends
		SchemaNode<E> {
	interface Effective<E extends Effective<E>> extends ChildNode<E>,
			SchemaNode.Effective<E> {
		Class<?> getPreInputClass();

		Class<?> getPostInputClass();

		void process(SchemaProcessingContext context);
	}
}
