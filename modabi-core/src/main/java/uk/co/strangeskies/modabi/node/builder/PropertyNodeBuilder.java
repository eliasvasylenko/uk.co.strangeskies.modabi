package uk.co.strangeskies.modabi.node.builder;

import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.node.PropertyNode;

public interface PropertyNodeBuilder<T> extends
		SchemaNodeBuilder<PropertyNode<T>> {
	PropertyNodeBuilder<T> name(String name);

	<U extends T> DataNodeBuilder<U> type(DataType<U> type);

	DataNodeBuilder<T> optional(boolean optional);

	DataNodeBuilder<T> data(T data);
}
