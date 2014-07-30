package uk.co.strangeskies.modabi.model.nodes;

import java.util.List;

public interface SchemaNode<E extends SchemaNode.Effective<E>> {
	interface Effective<E extends Effective<E>> extends SchemaNode<E> {
		@Override
		List<? extends ChildNode.Effective<?>> children();

		@SuppressWarnings("unchecked")
		@Override
		default E effective() {
			return (E) this;
		}
	}

	String getId();

	List<? extends ChildNode<?>> children();

	E effective();
}
