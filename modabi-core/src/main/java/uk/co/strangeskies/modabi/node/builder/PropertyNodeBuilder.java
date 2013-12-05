package uk.co.strangeskies.modabi.node.builder;

import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.node.PropertyNode;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public interface PropertyNodeBuilder<T> extends
		SchemaNodeBuilder<PropertyNode<T>, SchemaProcessingContext<?>> {
	PropertyNodeBuilder<T> name(String name);

	<U extends T> DataNodeBuilder<U> type(DataType<U> type);

	DataNodeBuilder<T> optional(boolean optional);

	DataNodeBuilder<T> data(T data);
}
