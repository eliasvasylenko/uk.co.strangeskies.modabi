package uk.co.strangeskies.modabi.schema.node.builder;

import uk.co.strangeskies.modabi.schema.node.DataSchemaNode;
import uk.co.strangeskies.modabi.schema.node.data.SchemaDataType;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface DataSchemaNodeBuilder<T> extends
		SchemaNodeBuilder<DataSchemaNode<T>, SchemaProcessingContext<?>> {
	public <U extends T> DataSchemaNodeBuilder<U> type(SchemaDataType<U> type);

	public DataSchemaNodeBuilder<T> optional(boolean optional);

	public DataSchemaNodeBuilder<T> data(T data);
}
