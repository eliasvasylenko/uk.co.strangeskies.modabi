package uk.co.strangeskies.modabi.schema.node;

import uk.co.strangeskies.modabi.schema.node.data.DataType;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface DataNode<T> extends InputNode<SchemaProcessingContext<?>> {
	public DataType<T> getType();

	public boolean isOptional();

	public boolean isDataSet();

	public T getData();
}
