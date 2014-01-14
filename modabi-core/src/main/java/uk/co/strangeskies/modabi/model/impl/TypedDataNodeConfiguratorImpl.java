package uk.co.strangeskies.modabi.model.impl;

import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.model.TypedDataNode;
import uk.co.strangeskies.modabi.model.building.TypedDataNodeConfigurator;

public abstract class TypedDataNodeConfiguratorImpl<S extends TypedDataNodeConfigurator<S, N, T>, N extends TypedDataNode<T>, T>
		extends SchemaNodeConfiguratorImpl<S, N> implements
		TypedDataNodeConfigurator<S, N, T>, TypedDataNode<T> {
	private Class<T> dataClass;

	private boolean iterable;
	private String outMethodName;
	private String inMethodName;
	private boolean inMethodChained;

	private DataType<T> type;
	private T value;

	public TypedDataNodeConfiguratorImpl(NodeBuilderContext context) {
		super(context);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> TypedDataNodeConfigurator<?, ?, V> dataClass(
			Class<V> dataClass) {
		this.dataClass = (Class<T>) dataClass;

		return (TypedDataNodeConfigurator<?, ?, V>) this;
	}

	public final Class<T> getDataClass() {
		return dataClass;
	}

	@Override
	public final S inMethod(String inMethodName) {
		this.inMethodName = inMethodName;

		return getThis();
	}

	public final String getInMethod() {
		return inMethodName;
	}

	@Override
	public final S inMethodChained(boolean chained) {
		this.inMethodChained = chained;

		return getThis();
	}

	public final boolean isInMethodChained() {
		return inMethodChained;
	}

	@Override
	public final S outMethod(String outMethodName) {
		this.outMethodName = outMethodName;

		return getThis();
	}

	public final String getOutMethod() {
		return outMethodName;
	}

	@Override
	public final S outMethodIterable(boolean iterable) {
		this.iterable = iterable;

		return getThis();
	}

	public final boolean isOutMethodIterable() {
		return iterable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U extends T> TypedDataNodeConfigurator<?, ?, U> type(DataType<U> type) {
		this.type = (DataType<T>) type;

		return (TypedDataNodeConfigurator<?, ?, U>) getThis();
	}

	public final DataType<T> getType() {
		return type;
	}

	@Override
	public final S value(T data) {
		this.value = data;

		return getThis();
	}

	public final T getValue() {
		return value;
	}

	@Override
	public boolean isValueSet() {
		return value != null;
	}
}
