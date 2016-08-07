package uk.co.strangeskies.modabi.schema;

public interface RootNode<T, S extends RootNode<T, S>> extends BindingNode<T, S> {
	boolean export();

	@Override
	default S root() {
		return getThis();
	}
}
