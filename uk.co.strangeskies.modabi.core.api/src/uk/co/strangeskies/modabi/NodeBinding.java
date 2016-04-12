package uk.co.strangeskies.modabi;

import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.reflection.TypedObject;

public abstract class NodeBinding<T, N extends BindingNode.Effective<T, ?, ?>> {
	private final N node;
	private final T data;

	public NodeBinding(N node, T data) {
		this.node = node;
		this.data = data;
	}

	public N getNode() {
		return node;
	}

	public T getData() {
		return data;
	}

	public TypedObject<T> getTypedData() {
		return TypedObject.castInto(node.getDataType(), data);
	}

	@Override
	public String toString() {
		return data + " : " + node;
	}

	// public void updateData();

	// public StructuredDataSource getSource();

	// public void updateSource();
}
