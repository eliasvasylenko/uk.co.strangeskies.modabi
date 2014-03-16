package uk.co.strangeskies.modabi.model.nodes;

import uk.co.strangeskies.modabi.data.DataType;

public interface TypedDataNode<T> extends DataNode<T> {
	public DataType<T> getType();

	public boolean isValueSet();

	public T getValue();
}
