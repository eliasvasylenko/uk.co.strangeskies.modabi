package uk.co.strangeskies.modabi.schema.node.builder;

import uk.co.strangeskies.modabi.schema.node.PropertySchemaNode;
import uk.co.strangeskies.modabi.schema.node.data.SchemaDataType;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface PropertySchemaNodeBuilder<T> extends
		SchemaNodeBuilder<PropertySchemaNode<T>, SchemaProcessingContext<?>> {
	PropertySchemaNodeBuilder<T> name(String name);

	<U extends T> DataSchemaNodeBuilder<U> type(SchemaDataType<U> type);

	DataSchemaNodeBuilder<T> optional(boolean optional);

	DataSchemaNodeBuilder<T> data(T data);
}
