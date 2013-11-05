package uk.co.strangeskies.modabi.schema.node.impl;

import uk.co.strangeskies.modabi.schema.node.DataSchemaNode;
import uk.co.strangeskies.modabi.schema.node.data.DataNodeType;

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
