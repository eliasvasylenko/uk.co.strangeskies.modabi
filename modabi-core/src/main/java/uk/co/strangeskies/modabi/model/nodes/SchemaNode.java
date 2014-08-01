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

		@Override
		default boolean equalsImpl(Object obj) {
			return false;
		}
	}

	String getName();

	List<? extends ChildNode<?>> children();

	E effective();

	default boolean equalsImpl(Object obj) {
		return false;
	}

	default int hashCodeImpl() {
		return 0;
	}
}
