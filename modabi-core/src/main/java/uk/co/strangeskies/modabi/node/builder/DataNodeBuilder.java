package uk.co.strangeskies.modabi.node.builder;

import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.node.DataNode;

public interface DataNodeBuilder<T> extends SchemaNodeBuilder<DataNode<T>> {
	public <U extends T> DataNodeBuilder<U> type(DataType<U> type);

	public DataNodeBuilder<T> optional(boolean optional);

	public DataNodeBuilder<T> data(T data);
}
