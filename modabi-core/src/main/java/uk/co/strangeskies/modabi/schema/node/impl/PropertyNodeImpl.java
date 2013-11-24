package uk.co.strangeskies.modabi.schema.node.impl;

import uk.co.strangeskies.modabi.schema.data.DataType;
import uk.co.strangeskies.modabi.schema.node.PropertyNode;

public class PropertyNodeImpl<T> extends DataNodeImpl<T> implements
		PropertyNode<T> {
	private final String name;

	public PropertyNodeImpl(String name, DataType<T> type, T data,
			boolean optional) {
		super(type, data, optional);

		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}
