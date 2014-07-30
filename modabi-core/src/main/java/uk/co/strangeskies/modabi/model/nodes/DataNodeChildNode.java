package uk.co.strangeskies.modabi.model.nodes;

public interface DataNodeChildNode<E extends DataNodeChildNode<E> & ChildNode.Effective<E>>
		extends ChildNode<E> {
}
