package uk.co.strangeskies.modabi.schema.node;

import uk.co.strangeskies.modabi.schema.processing.DataInput;

public interface SchemaNode<T> {
	public T unmarshall(DataInput context);

	public void marshall(DataOutput context, T object);
}
