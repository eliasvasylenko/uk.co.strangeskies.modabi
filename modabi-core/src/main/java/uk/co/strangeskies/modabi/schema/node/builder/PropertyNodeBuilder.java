package uk.co.strangeskies.modabi.schema.node.builder;

import uk.co.strangeskies.modabi.schema.node.PropertyNode;
import uk.co.strangeskies.modabi.schema.node.data.DataType;
import uk.co.strangeskies.modabi.schema.processing.DataInput;

public interface PropertyNodeBuilder<T> extends
		SchemaNodeBuilder<PropertyNode<T>, DataInput<?>> {
	PropertyNodeBuilder<T> name(String name);

	<U extends T> DataNodeBuilder<U> type(DataType<U> type);

	DataNodeBuilder<T> optional(boolean optional);

	DataNodeBuilder<T> data(T data);
}
