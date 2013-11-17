package uk.co.strangeskies.modabi.schema.node.impl;

import uk.co.strangeskies.modabi.schema.node.DataSchemaNode;
import uk.co.strangeskies.modabi.schema.node.data.SchemaDataType;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public class DataSchemaNodeImpl<T> implements DataSchemaNode<T> {
	private final SchemaDataType<T> type;
	private final T data;
	private final boolean optional;

	public DataSchemaNodeImpl(SchemaDataType<T> type, T data, boolean optional) {
		this.type = type;
		this.data = data;
		this.optional = optional;
	}

	@Override
	public SchemaDataType<T> getType() {
		return type;
	}

	@Override
	public boolean isOptional() {
		return optional;
	}

	@Override
	public void process(SchemaProcessingContext<?> context) {
		context.data(this);
	}

	@Override
	public boolean isDataSet() {
		return data != null;
	}

	@Override
	public T getData() {
		return data;
	}
}
