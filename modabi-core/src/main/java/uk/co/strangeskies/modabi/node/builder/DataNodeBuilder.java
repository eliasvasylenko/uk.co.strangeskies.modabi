package uk.co.strangeskies.modabi.node.builder;

import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.node.DataNode;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public interface DataNodeBuilder<T> extends
		SchemaNodeBuilder<DataNode<T>, SchemaProcessingContext<?>> {
	public <U extends T> DataNodeBuilder<U> type(DataType<U> type);

	public DataNodeBuilder<T> optional(boolean optional);

	public DataNodeBuilder<T> data(T data);
}
