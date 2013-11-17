package uk.co.strangeskies.modabi.schema.node;

import uk.co.strangeskies.modabi.schema.node.data.SchemaDataType;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface DataSchemaNode<T> extends
		SchemaNode<SchemaProcessingContext<?>> {
	public SchemaDataType<T> getType();

	public boolean isOptional();

	public boolean isDataSet();

	public T getData();
}
