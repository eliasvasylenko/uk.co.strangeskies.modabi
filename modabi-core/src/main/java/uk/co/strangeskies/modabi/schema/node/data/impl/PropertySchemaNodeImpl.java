package uk.co.strangeskies.modabi.schema.node.data.impl;

import uk.co.strangeskies.modabi.schema.node.data.DataNodeType;
import uk.co.strangeskies.modabi.schema.node.data.PropertySchemaNode;

public class PropertySchemaNodeImpl extends DataSchemaNodeImpl implements
		PropertySchemaNode {
	private final String name;

	public PropertySchemaNodeImpl(String name, DataNodeType<?> type,
			boolean optional) {
		super(type, optional);

		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}
