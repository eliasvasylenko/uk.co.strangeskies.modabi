package uk.co.strangeskies.modabi.schema.node;


public interface DataNodeBindingChildNode<T, S extends DataNodeBindingChildNode<T, S, E>, E extends DataNodeBindingChildNode<T, S, E> & DataNodeChildNode<S, E> & BindingChildNode.Effective<T, S, E>>
		extends DataNodeChildNode<S, E>, BindingChildNode<T, S, E> {
}
