package uk.co.strangeskies.modabi.schema.node.impl;

import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.data.DataType;
import uk.co.strangeskies.modabi.schema.processing.DataInput;

public class DataNodeImpl<T> implements DataNode<T> {
	private final DataType<T> type;
	private final T data;
	private final boolean optional;

	public DataNodeImpl(DataType<T> type, T data, boolean optional) {
		this.type = type;
		this.data = data;
		this.optional = optional;
	}

	@Override
	public DataType<T> getType() {
		return type;
	}

	@Override
	public boolean isOptional() {
		return optional;
	}

	@Override
	public void process(DataInput<?> context) {
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

	@Override
	public String getInMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isInMethodChained() {
		// TODO Auto-generated method stub
		return false;
	}
}