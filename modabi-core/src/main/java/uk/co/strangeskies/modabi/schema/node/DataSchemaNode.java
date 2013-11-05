package uk.co.strangeskies.modabi.schema.node;

import uk.co.strangeskies.modabi.schema.node.data.DataNodeType;

public interface DataSchemaNode extends SchemaNode {
	public DataNodeType<?> getType();

	public boolean isOptional();
}
