package uk.co.strangeskies.modabi.schema.node.impl;

import uk.co.strangeskies.modabi.schema.node.PropertySchemaNode;
import uk.co.strangeskies.modabi.schema.node.data.DataNodeType;

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
