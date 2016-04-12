package uk.co.strangeskies.modabi;

import uk.co.strangeskies.modabi.schema.BindingChildNode;

public class ChildNodeBinding<T> extends NodeBinding<T, BindingChildNode.Effective<T, ?, ?>> {
	public ChildNodeBinding(BindingChildNode.Effective<T, ?, ?> node, T data) {
		super(node, data);
	}
}
