package uk.co.strangeskies.modabi.node;

import uk.co.strangeskies.modabi.data.DataType;

public interface DataNode<T> extends InputNode {
	public DataType<T> getType();

	public boolean isOptional();

	public boolean isDataSet();

	public T getData();
}
