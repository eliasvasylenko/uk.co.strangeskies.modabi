package uk.co.strangeskies.modabi.model.nodes;

public interface DataNodeBindingChildNode<T, E extends DataNodeBindingChildNode<T, E> & DataNodeChildNode<E> & BindingChildNode.Effective<T, E>>
		extends DataNodeChildNode<E>, BindingChildNode<T, E> {
}
