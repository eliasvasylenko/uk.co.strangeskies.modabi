package uk.co.strangeskies.modabi.model.impl;

import java.util.Collection;

import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.model.TypedDataNode;
import uk.co.strangeskies.modabi.model.building.TypedDataNodeConfigurator;

abstract class TypedDataNodeConfiguratorImpl<S extends TypedDataNodeConfigurator<S, N, T>, N extends TypedDataNode<T>, T>
		extends SchemaNodeConfiguratorImpl<S, N> implements
		TypedDataNodeConfigurator<S, N, T> {
	protected abstract static class TypedDataNodeImpl<T> extends SchemaNodeImpl
			implements TypedDataNode<T> {
		private final Class<T> dataClass;
		private final Boolean iterable;
		private final String outMethodName;
		private final String inMethodName;
		private final Boolean inMethodChained;
		private final DataType<T> type;
		private final T value;

		TypedDataNodeImpl(TypedDataNodeConfiguratorImpl<?, ?, T> configurator) {
			super(configurator);

			dataClass = configurator.dataClass;
			iterable = configurator.iterable;
			outMethodName = configurator.outMethodName;
			inMethodName = configurator.inMethodName;
			inMethodChained = configurator.inMethodChained;
			type = configurator.type;
			value = configurator.value;
		}

		@SuppressWarnings("unchecked")
		<E extends TypedDataNode<? super T>> TypedDataNodeImpl(E node,
				Collection<? extends E> overriddenNodes) {
			super(node, overriddenNodes);

			dataClass = getValue(node, overriddenNodes,
					n -> (Class<T>) n.getDataClass(), (v, o) -> o.isAssignableFrom(v));

			iterable = getValue(node, overriddenNodes, n -> n.isOutMethodIterable());

			outMethodName = getValue(node, overriddenNodes, n -> n.getOutMethod());

			inMethodName = getValue(node, overriddenNodes, n -> n.getInMethod());

			inMethodChained = getValue(node, overriddenNodes,
					n -> n.isInMethodChained());

			type = getValue(node, overriddenNodes, n -> (DataType<T>) n.getType());

			value = getValue(node, overriddenNodes, n -> (T) n.getValue());

			if (!dataClass.isAssignableFrom(value.getClass()))
				throw new SchemaException();
		}

		@Override
		protected void validateAsEffectiveModel(boolean isAbstract) {
			super.validateAsEffectiveModel(isAbstract);

			if (type == null)
				throw new SchemaException();
		}

		@Override
		public final String getOutMethod() {
			return outMethodName;
		}

		@Override
		public final Boolean isOutMethodIterable() {
			return iterable;
		}

		@Override
		public final Class<T> getDataClass() {
			return dataClass;
		}

		@Override
		public final String getInMethod() {
			return inMethodName;
		}

		@Override
		public final Boolean isInMethodChained() {
			return inMethodChained;
		}

		@Override
		public final DataType<T> getType() {
			return type;
		}

		@Override
		public final boolean isValueSet() {
			return value != null;
		}

		@Override
		public final T getValue() {
			return value;
		}
	}

	private Class<T> dataClass;
	private Boolean iterable;
	private String outMethodName;
	private String inMethodName;
	private Boolean inMethodChained;
	private DataType<T> type;
	private T value;

	public TypedDataNodeConfiguratorImpl(
			BranchingNodeConfiguratorImpl<?, ?> parent) {
		super(parent);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> TypedDataNodeConfigurator<?, ?, V> dataClass(
			Class<V> dataClass) {
		this.dataClass = (Class<T>) dataClass;

		return (TypedDataNodeConfigurator<?, ?, V>) this;
	}

	@Override
	public final S inMethod(String inMethodName) {
		this.inMethodName = inMethodName;

		return getThis();
	}

	@Override
	public final S inMethodChained(boolean chained) {
		this.inMethodChained = chained;

		return getThis();
	}

	@Override
	public final S outMethod(String outMethodName) {
		this.outMethodName = outMethodName;

		return getThis();
	}

	@Override
	public final S outMethodIterable(boolean iterable) {
		this.iterable = iterable;

		return getThis();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U extends T> TypedDataNodeConfigurator<?, ?, U> type(DataType<U> type) {
		this.type = (DataType<T>) type;
		this.dataClass = (Class<T>) type.getDataClass();

		return (TypedDataNodeConfigurator<?, ?, U>) getThis();
	}

	@Override
	public final S value(T data) {
		this.value = data;

		return getThis();
	}
}
