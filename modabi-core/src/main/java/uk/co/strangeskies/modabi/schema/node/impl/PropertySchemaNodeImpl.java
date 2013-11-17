package uk.co.strangeskies.modabi.schema.node.impl;

import uk.co.strangeskies.modabi.schema.node.PropertySchemaNode;
import uk.co.strangeskies.modabi.schema.node.data.SchemaDataType;

public class PropertySchemaNodeImpl<T> extends DataSchemaNodeImpl<T> implements
		PropertySchemaNode<T> {
	private final String name;

	public PropertySchemaNodeImpl(String name, SchemaDataType<T> type, T data,
			boolean optional) {
		super(type, data, optional);

		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}
