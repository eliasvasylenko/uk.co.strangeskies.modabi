package uk.co.strangeskies.modabi.model.impl;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.model.building.DataNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode.Format;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;
import uk.co.strangeskies.modabi.processing.SchemaResultProcessingContext;
import uk.co.strangeskies.modabi.processing.impl.SchemaBinderImpl;

public class DataNodeConfiguratorImpl<T> extends
		SchemaNodeConfiguratorImpl<DataNodeConfigurator<T>, DataNode<T>> implements
		DataNodeConfigurator<T> {
	protected static class DataNodeImpl<T> extends SchemaNodeImpl implements
			DataNode<T> {
		private final Class<T> dataClass;
		private final Boolean iterable;
		private final String outMethodName;
		private final Method outMethod;
		private final String inMethodName;
		private final Method inMethod;
		private final Boolean inMethodChained;
		private final DataType<T> type;
		private final T value;
		private final Format format;

		DataNodeImpl(DataNodeConfiguratorImpl<T> configurator) {
			super(configurator);

			format = configurator.format;

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
		<E extends DataNode<? super T>> DataNodeImpl(E node,
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

			type = getValue(node, overriddenNodes, n -> (DataType<T>) n.type());

			value = getValue(node, overriddenNodes, n -> (T) n.value());

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
		public final DataType<T> type() {
			return type;
		}

		@Override
		public final T value() {
			return value;
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

		@Override
		public void process(SchemaProcessingContext context) {
			// TODO Auto-generated method stub

		}

		@Override
		public <T> T process(SchemaResultProcessingContext<T> context) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Format format() {
			return format;
		}

		@Override
		public Range<Integer> occurances() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Boolean isOptional() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public Format format;

	private Class<T> dataClass;
	private Boolean iterable;
	private String outMethodName;
	private String inMethodName;
	private Boolean inMethodChained;
	private DataType<T> type;
	private T value;

	private final Class<?> preInputClass;
	private final Class<?> parentClass;

	private Object occurances;
	private Object optional;

	public DataNodeConfiguratorImpl(BranchingNodeConfiguratorImpl<?, ?> parent) {
		super(parent);
		preInputClass = parent.getCurrentChildPreInputClass();
		parentClass = parent.getDataClass();
	}

	public DataNodeConfiguratorImpl(
			SchemaNodeOverrideContext<DataNode<T>> overrideContext,
			SchemaNodeResultListener<DataNode<T>> resultListener,
			Class<?> preInputClass, Class<?> outputClass) {
		super(overrideContext, resultListener);
		this.preInputClass = preInputClass;
		this.parentClass = outputClass;
	}

	public Class<?> getParentClass() {
		return parentClass;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> DataNodeConfigurator<V> dataClass(Class<V> dataClass) {
		requireConfigurable(this.dataClass);
		this.dataClass = (Class<T>) dataClass;

		return (DataNodeConfigurator<V>) this;
	}

	@Override
	public final DataNodeConfigurator<T> inMethod(String inMethodName) {
		requireConfigurable(this.inMethodName);
		this.inMethodName = inMethodName;

		return getThis();
	}

	@Override
	public final DataNodeConfigurator<T> inMethodChained(boolean chained) {
		requireConfigurable(this.inMethodChained);
		this.inMethodChained = chained;

		return getThis();
	}

	@Override
	public final DataNodeConfigurator<T> outMethod(String outMethodName) {
		requireConfigurable(this.outMethodName);
		this.outMethodName = outMethodName;

		return getThis();
	}

	@Override
	public final DataNodeConfigurator<T> outMethodIterable(boolean iterable) {
		requireConfigurable(this.iterable);
		this.iterable = iterable;

		return getThis();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U extends T> DataNodeConfigurator<U> type(DataType<U> type) {
		requireConfigurable(this.type);
		requireConfigurable(this.dataClass);
		this.type = (DataType<T>) type;
		this.dataClass = (Class<T>) type.getDataClass();

		return (DataNodeConfigurator<U>) getThis();
	}

	@Override
	public final DataNodeConfigurator<T> value(T data) {
		requireConfigurable(this.value);
		this.value = data;

		return getThis();
	}

	@Override
	public final DataNodeConfigurator<T> occurances(Range<Integer> range) {
		requireConfigurable(occurances);
		occurances = range;

		return this;
	}

	@Override
	public DataNodeConfigurator<T> optional(boolean optional) {
		requireConfigurable(this.optional);
		this.optional = optional;

		return this;
	}

	@Override
	public DataNodeConfigurator<T> format(Format format) {
		requireConfigurable(this.format);
		this.format = format;

		return this;
	}

	@Override
	protected DataNode<T> getEffective(DataNode<T> node) {
		return new DataNodeImpl<>(node, getOverriddenNodes(), getParentClass());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Class<DataNode<T>> getNodeClass() {
		return (Class<DataNode<T>>) (Object) DataNode.class;
	}

	@Override
	protected DataNode<T> tryCreate() {
		return new DataNodeImpl<>(this);
	}
}
