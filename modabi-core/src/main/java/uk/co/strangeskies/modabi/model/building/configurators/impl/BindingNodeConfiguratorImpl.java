package uk.co.strangeskies.modabi.model.building.configurators.impl;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import uk.co.strangeskies.modabi.model.building.configurators.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.BindingNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;

public abstract class BindingNodeConfiguratorImpl<S extends BindingNodeConfigurator<S, N, T>, N extends BindingNode<T>, T, C extends ChildNode, B extends BindingChildNode<?>>
		extends SchemaNodeConfiguratorImpl<S, N, C, B> implements
		BindingNodeConfigurator<S, N, T> {
	protected abstract static class BindingNodeImpl<T> extends SchemaNodeImpl
			implements BindingNode<T> {
		private final Class<T> dataClass;
		private final Class<?> bindingClass;
		private final Class<?> unbindingClass;
		private final BindingStrategy bindingStrategy;
		private final UnbindingStrategy unbindingStrategy;
		private final String unbindingMethodName;
		private final Method unbindingMethod;

		public BindingNodeImpl(
				BindingNodeConfiguratorImpl<?, ?, T, ?, ?> configurator) {
			super(configurator);

			dataClass = configurator.dataClass;

			bindingStrategy = configurator.bindingStrategy;
			bindingClass = configurator.bindingClass;

			unbindingStrategy = configurator.unbindingStrategy;
			unbindingClass = configurator.unbindingClass;
			unbindingMethodName = configurator.unbindingMethod;

			unbindingMethod = findUnbindingMethod(getId(), getUnbindingStrategy(),
					getUnbindingMethodName(), getUnbindingClass(), getDataClass());
		}

		@SuppressWarnings("unchecked")
		public BindingNodeImpl(BindingNode<T> node,
				Collection<? extends BindingNode<? super T>> overriddenNodes,
				List<ChildNode> effectiveChildren) {
			super(node, overriddenNodes, effectiveChildren);

			OverrideMerge<BindingNode<? super T>> overrideMerge = new OverrideMerge<>(
					node, overriddenNodes);

			dataClass = (Class<T>) overrideMerge.getValue(n -> n.getDataClass(), (v,
					o) -> o.isAssignableFrom(v));

			bindingClass = overrideMerge.getValue(n -> n.getBindingClass());

			unbindingClass = overrideMerge.getValue(n -> n.getUnbindingClass());

			bindingStrategy = overrideMerge.getValue(n -> n.getBindingStrategy());

			unbindingStrategy = overrideMerge.getValue(n -> n.getUnbindingStrategy());

			unbindingMethodName = overrideMerge.getValue(
					n -> n.getUnbindingMethodName(), (o, v) -> o.equals(v));

			unbindingMethod = overrideMerge.getValue(n -> n.getUnbindingMethod());
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof BindingNode))
				return false;

			BindingNode<?> other = (BindingNode<?>) obj;
			return super.equals(obj)
					&& Objects.equals(dataClass, other.getDataClass())
					&& Objects.equals(bindingClass, other.getBindingClass())
					&& Objects.equals(unbindingClass, other.getUnbindingClass())
					&& Objects.equals(bindingStrategy, other.getBindingStrategy())
					&& Objects.equals(unbindingStrategy, other.getUnbindingStrategy())
					&& Objects
							.equals(unbindingMethodName, other.getUnbindingMethodName())
					&& Objects.equals(unbindingMethod, other.getUnbindingMethod());
		}

		@Override
		public Class<T> getDataClass() {
			return dataClass;
		}

		@Override
		public BindingStrategy getBindingStrategy() {
			return bindingStrategy;
		}

		@Override
		public UnbindingStrategy getUnbindingStrategy() {
			return unbindingStrategy;
		}

		@Override
		public Class<?> getBindingClass() {
			return bindingClass;
		}

		@Override
		public Class<?> getUnbindingClass() {
			return unbindingClass;
		}

		@Override
		public Method getUnbindingMethod() {
			return unbindingMethod;
		}

		@Override
		public String getUnbindingMethodName() {
			return unbindingMethodName;
		}
	}

	private Class<T> dataClass;

	private BindingStrategy bindingStrategy;
	private Class<?> bindingClass;

	private UnbindingStrategy unbindingStrategy;
	private Class<?> unbindingClass;
	public String unbindingMethod;

	private Class<?> unbindingStaticFactoryClass;

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> BindingNodeConfigurator<?, ?, V> dataClass(
			Class<V> dataClass) {
		requireConfigurable(this.dataClass);
		this.dataClass = (Class<T>) dataClass;

		return (BindingNodeConfigurator<?, ?, V>) this;
	}

	protected UnbindingStrategy getUnbindingStrategy() {
		return unbindingStrategy;
	}

	protected String getUnbindingMethod() {
		return unbindingMethod;
	}

	protected BindingStrategy getBindingStrategy() {
		return bindingStrategy;
	}

	protected Class<T> getDataClass() {
		return dataClass;
	}

	@Override
	protected final Class<?> getCurrentChildOutputTargetClass() {
		if (getUnbindingStrategy() == null
				|| getUnbindingStrategy() == UnbindingStrategy.SIMPLE)
			return getDataClass();
		else if (getUnbindingStrategy() == UnbindingStrategy.STATIC_FACTORY)
			try {
				return getUnbindingClass().getMethod(getUnbindingMethod(),
						getDataClass()).getReturnType();
			} catch (NoSuchMethodException | SecurityException e) {
				throw new SchemaException(e);
			}
		return getUnbindingClass() != null ? getUnbindingClass() : getDataClass();
	}

	@Override
	protected Class<?> getCurrentChildInputTargetClass() {
		if (getChildren().getChildren().isEmpty())
			return getBindingClass() != null ? getBindingClass() : getDataClass();
		else
			return getChildren().getChildren()
					.get(getChildren().getChildren().size() - 1).getPostInputClass();
	}

	@Override
	public final S bindingClass(Class<?> bindingClass) {
		requireConfigurable(this.bindingClass);
		this.bindingClass = bindingClass;

		return getThis();
	}

	protected Class<?> getBindingClass() {
		return bindingClass;
	}

	@Override
	public S unbindingClass(Class<?> unbindingClass) {
		requireConfigurable(this.unbindingClass);
		this.unbindingClass = unbindingClass;

		return getThis();
	}

	protected Class<?> getUnbindingClass() {
		return unbindingClass;
	}

	@Override
	public S unbindingMethod(String unbindingMethod) {
		requireConfigurable(this.unbindingMethod);
		this.unbindingMethod = unbindingMethod;

		return getThis();
	}

	protected final Class<?> getBuilderClass() {
		return bindingClass;
	}

	@Override
	public final S bindingStrategy(BindingStrategy strategy) {
		requireConfigurable(bindingStrategy);
		bindingStrategy = strategy;

		return getThis();
	}

	@Override
	public final S unbindingStrategy(UnbindingStrategy strategy) {
		requireConfigurable(unbindingStrategy);
		unbindingStrategy = strategy;

		return getThis();
	}

	@Override
	public S unbindingStaticFactoryClass(Class<?> factoryClass) {
		requireConfigurable(unbindingStaticFactoryClass);
		unbindingStaticFactoryClass = factoryClass;

		return getThis();
	}

	private Method getOutMethod(Class<?> targetClass,
			OverrideMerge<BindingChildNode<? super T>> overrideMerge) {
		try {
			Method overriddenMethod = overrideMerge.getValue(n -> n.getOutMethod(), (
					m, n) -> m.equals(n));

			Class<?> resultClass = (isOutMethodIterable() != null && isOutMethodIterable()) ? Iterable.class
					: getDataClass();

			Class<?> receiverClass = null;
			Class<?>[] parameters = new Class<?>[0];
			UnbindingStrategy unbindingStrategy = getUnbindingStrategy() == null ? UnbindingStrategy.SIMPLE
					: getUnbindingStrategy();
			switch (unbindingStrategy) {
			case CONSTRUCTOR:
			case PASS_TO_PROVIDED:
			case STATIC_FACTORY:
				parameters = new Class<?>[] { targetClass };
				receiverClass = getUnbindingClass() != null ? getUnbindingClass()
						: getDataClass();
				break;
			case ACCEPT_PROVIDED:
				parameters = new Class<?>[] { getUnbindingClass() != null ? getUnbindingClass()
						: getDataClass() };
			case SIMPLE:
				receiverClass = targetClass;
			}

			return (targetClass == null || resultClass == null || outMethodName == "this") ? null
					: overriddenMethod != null ? overriddenMethod
							: BindingNodeConfigurator.findMethod(BindingNodeConfigurator
									.generateOutMethodNames(this, resultClass), receiverClass,
									resultClass, parameters);
		} catch (NoSuchMethodException e) {
			throw new SchemaException(e);
		}
	}

	public static Method findUnbindingMethod(String propertyName,
			UnbindingStrategy unbindingStrategy, String unbindingMethodName,
			Class<?> unbindingClass, Class<?> dataClass, Class<?> factoryClass) {
		if (dataClass == null)
			return null;

		if (unbindingStrategy == null)
			unbindingStrategy = UnbindingStrategy.SIMPLE;

		Class<?> resultClass = null;
		Class<?> targetClass = null;
		Class<?>[] parameters = new Class<?>[0];

		switch (unbindingStrategy) {
		case SIMPLE:
			if (unbindingClass != null)
				throw new SchemaException();
		case CONSTRUCTOR:
			if (unbindingMethodName != null)
				throw new SchemaException();
			return null;
		case STATIC_FACTORY:
		case PROVIDED_FACTORY:
			targetClass = factoryClass != null ? factoryClass : unbindingClass;
			parameters = new Class<?>[] { dataClass };
			resultClass = unbindingClass;
			break;
		case PASS_TO_PROVIDED:
			targetClass = unbindingClass;
			parameters = new Class<?>[] { dataClass };
			break;
		case ACCEPT_PROVIDED:
			targetClass = dataClass;
			if (unbindingClass == null)
				throw new SchemaException();
			parameters = new Class<?>[] { unbindingClass };
		}
		try {

			if (unbindingStrategy == UnbindingStrategy.ACCEPT_PROVIDED)
				unbindingMethod = BindingNodeConfigurator.findMethod(names, dataClass,
						resultClass, unbindingClass);
			else
				unbindingMethod = BindingNodeConfigurator.findMethod(names,
						unbindingClass, resultClass, dataClass);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new SchemaException(e);
		}
		return unbindingMethod;
	}

	List<String> getNames(String propertyName, String unbindingMethodName,
			Class<?> resultClass) {
		List<String> names;
		if (unbindingMethodName != null)
			names = Arrays.asList(unbindingMethodName);
		else {
			names = BindingNodeConfigurator.generateOutMethodNames(propertyName,
					false, resultClass);
		}
		return names;
	}
}
