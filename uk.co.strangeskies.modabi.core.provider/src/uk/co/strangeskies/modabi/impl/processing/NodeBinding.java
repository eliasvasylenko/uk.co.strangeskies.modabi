package uk.co.strangeskies.modabi.impl.processing;

import uk.co.strangeskies.modabi.schema.BindingChildNode;
import uk.co.strangeskies.reflection.TypedObject;

public class NodeBinding<U> {
	private final U binding;
	private final BindingChildNode.Effective<? extends U, ?, ?> exactNode;

	public NodeBinding(U binding, BindingChildNode.Effective<? extends U, ?, ?> exactNode) {
		this.binding = binding;
		this.exactNode = exactNode;
	}

	public U getBinding() {
		return binding;
	}

	public BindingChildNode.Effective<? extends U, ?, ?> getExactNode() {
		return exactNode;
	}

	public TypedObject<? extends U> getTypedBinding() {
		return TypedObject.castInto(exactNode.getDataType(), binding);
	}
}
