package uk.co.strangeskies.modabi.schema.node.builder;

import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.data.DataType;
import uk.co.strangeskies.modabi.schema.processing.DataInput;

public interface DataNodeBuilder<T> extends
		SchemaNodeBuilder<DataNode<T>, DataInput<?>> {
	public <U extends T> DataNodeBuilder<U> type(DataType<U> type);

	public DataNodeBuilder<T> optional(boolean optional);

	public DataNodeBuilder<T> data(T data);
}
