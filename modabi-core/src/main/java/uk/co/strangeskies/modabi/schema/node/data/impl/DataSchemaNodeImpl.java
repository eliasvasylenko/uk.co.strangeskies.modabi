package uk.co.strangeskies.modabi.schema.node.data.impl;

import uk.co.strangeskies.modabi.schema.node.data.DataNodeType;
import uk.co.strangeskies.modabi.schema.node.data.DataSchemaNode;

public class DataSchemaNodeImpl implements DataSchemaNode {
	private final DataNodeType<?> type;
	private final boolean optional;

	public DataSchemaNodeImpl(DataNodeType<?> type, boolean optional) {
		this.type = type;
		this.optional = optional;
	}

	@Override
	public DataNodeType<?> getType() {
		return type;
	}

	@Override
	public boolean isOptional() {
		return optional;
	}
}
