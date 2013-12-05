package uk.co.strangeskies.modabi.node;

import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public interface DataNode<T> extends InputNode<SchemaProcessingContext<?>> {
	public DataType<T> getType();

	public boolean isOptional();

	public boolean isDataSet();

	public T getData();
}
