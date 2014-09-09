package uk.co.strangeskies.modabi.schema.model.nodes;

public interface DataNodeChildNode<S extends DataNodeChildNode<S, E>, E extends DataNodeChildNode<S, E> & ChildNode.Effective<S, E>>
		extends ChildNode<S, E> {
}
