package uk.co.strangeskies.modabi.schema.node;

import uk.co.strangeskies.modabi.schema.processing.DataInput;

public interface SchemaNode {
	public void process(DataInput context);

	public void process(DataOutput context);
}
