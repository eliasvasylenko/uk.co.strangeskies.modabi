package uk.co.strangeskies.modabi.schema.node;

import uk.co.strangeskies.modabi.schema.node.data.DataType;
import uk.co.strangeskies.modabi.schema.processing.DataInput;

public interface DataNode<T> extends InputNode<DataInput<?>> {
	public DataType<T> getType();

	public boolean isOptional();

	public boolean isDataSet();

	public T getData();
}
