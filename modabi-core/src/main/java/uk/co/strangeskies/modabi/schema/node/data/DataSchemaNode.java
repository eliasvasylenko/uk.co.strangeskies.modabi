package uk.co.strangeskies.modabi.schema.node.data;

import uk.co.strangeskies.modabi.schema.node.SchemaNode;

public interface DataSchemaNode extends SchemaNode {
	public DataNodeType<?> getType();

	public boolean isOptional();
}
