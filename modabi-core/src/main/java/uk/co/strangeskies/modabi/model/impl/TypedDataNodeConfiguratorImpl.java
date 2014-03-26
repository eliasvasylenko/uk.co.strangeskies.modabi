package uk.co.strangeskies.modabi.model.impl;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.model.building.DataNodeConfigurator;
import uk.co.strangeskies.modabi.model.children.ChildNode;
import uk.co.strangeskies.modabi.model.children.DataNode;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.UnbindingStrategy;
import uk.co.strangeskies.modabi.processing.impl.SchemaBinderImpl;

public abstract class TypedDataNodeConfiguratorImpl<S extends DataNodeConfigurator<S, N, T>, N extends DataNode<T>, T>
		extends SchemaNodeConfiguratorImpl<S, N> implements
		DataNodeConfigurator<S, N, T> {
	protected static abstract class TypedDataNodeImpl<T> extends SchemaNodeImpl
			implements DataNode<T> {
		private final Class<T> dataClass;
		private final Boolean iterable;
		private final String outMethodName;
		private final Method outMethod;
		private final String inMethodName;
		private final Method inMethod;
		private final Boolean inMethodChained;
		private final DataType<T> type;
		private final T value;

		TypedDataNodeImpl(DataNodeConfiguratorImpl<?, ?, T> configurator) {
			super(configurator);

			dataClass = configurator.dataClass;
			iterable = configurator.iterable;
			outMethodName = configurator.outMethodName;
			inMethodName = configurator.inMethodName;
			try {
				Class<?> outputClass = configurator.parentClass;
				outMethod = (outputClass == null || dataClass == null) ? null
						: SchemaBinderImpl.findMethod(
								SchemaBinderImpl.generateOutMethodNames(this), outputClass,
								dataClass);

				Class<?> inputClass = configurator.preInputClass;
				inMethod = (inputClass == null || dataClass == null || inMethodName == null) ? null
						: inputClass.getMethod(inMethodName, dataClass);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new SchemaException(e);
			}
			inMethodChained = configurator.inMethodChained;
			type = configurator.type;
			value = configurator.value;
		}

		@SuppressWarnings("unchecked")
		<E extends DataNode<? super T>> TypedDataNodeImpl(E node,
				Collection<? extends E> overriddenNodes, Class<?> parentClass) {
			super(node, overriddenNodes);

			dataClass = getValue(node, overriddenNodes,
					n -> (Class<T>) n.getDataClass(), (v, o) -> o.isAssignableFrom(v));

			iterable = getValue(node, overriddenNodes, n -> n.isOutMethodIterable(),
					(n, o) -> Objects.equals(n, o));

			outMethodName = getValue(node, overriddenNodes, n -> n.getOutMethodName());

			Method inheritedOutMethod = getValue(node, overriddenNodes,
					n -> n.getOutMethod());
			try {
				outMethod = inheritedOutMethod != null ? inheritedOutMethod
						: SchemaBinderImpl.findMethod(
								SchemaBinderImpl.generateOutMethodNames(this), parentClass,
								dataClass);
			} catch (NoSuchMethodException e) {
				throw new SchemaException(e);
			}

			inMethodName = getValue(node, overriddenNodes, n -> n.getInMethodName());

			inMethod = getValue(node, overriddenNodes, n -> n.getInMethod(),
					(m, n) -> m.equals(n));

			inMethodChained = getValue(node, overriddenNodes,
					n -> n.isInMethodChained());

			type = getValue(node, overriddenNodes, n -> (DataType<T>) n.getType());

			value = getValue(node, overriddenNodes, n -> (T) n.getValue());

			if (value != null && !dataClass.isAssignableFrom(value.getClass()))
				throw new SchemaException();
		}

		@Override
		public final String getOutMethodName() {
			return outMethodName;
		}

		@Override
		public final Method getOutMethod() {
			return outMethod;
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
		public final String getInMethodName() {
			return inMethodName;
		}

		@Override
		public final Method getInMethod() {
			return inMethod;
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

		@Override
		public BindingStrategy getBindingStrategy() {
			return type.getBindingStrategy();
		}

		@Override
		public Class<?> getBindingClass() {
			return type.getBindingClass();
		}

		@Override
		public UnbindingStrategy getUnbindingStrategy() {
			return type.getUnbindingStrategy();
		}

		@Override
		public Class<?> getUnbindingClass() {
			return type.getUnbindingClass();
		}

		@Override
		public Method getUnbindingMethod() {
			return type.getUnbindingMethod();
		}

		@Override
		public List<? extends ChildNode> getChildren() {
			return type.getProperties();
		}

		@Override
		public Class<?> getPreInputClass() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Class<?> getPostInputClass() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	private Class<T> dataClass;
	private Boolean iterable;
	private String outMethodName;
	private String inMethodName;
	private Boolean inMethodChained;
	private DataType<T> type;
	private T value;

	private final Class<?> preInputClass;
	private final Class<?> parentClass;

	public TypedDataNodeConfiguratorImpl(
			BranchingNodeConfiguratorImpl<?, ?> parent) {
		super(parent);
		preInputClass = parent.getCurrentChildPreInputClass();
		parentClass = parent.getDataClass();
	}

	public TypedDataNodeConfiguratorImpl(
			SchemaNodeOverrideContext<N> overrideContext,
			SchemaNodeResultListener<N> resultListener, Class<?> preInputClass,
			Class<?> outputClass) {
		super(overrideContext, resultListener);
		this.preInputClass = preInputClass;
		this.parentClass = outputClass;
	}

	public Class<?> getParentClass() {
		return parentClass;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> DataNodeConfigurator<?, ?, V> dataClass(
			Class<V> dataClass) {
		this.dataClass = (Class<T>) dataClass;

		return (DataNodeConfigurator<?, ?, V>) this;
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
	public <U extends T> DataNodeConfigurator<?, ?, U> type(DataType<U> type) {
		this.type = (DataType<T>) type;
		this.dataClass = (Class<T>) type.getDataClass();

		return (DataNodeConfigurator<?, ?, U>) getThis();
	}

	@Override
	public final S value(T data) {
		this.value = data;

		return getThis();
	}
}
