package uk.co.strangeskies.modabi.model.building.impl;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import uk.co.strangeskies.modabi.model.building.BindingNodeConfigurator;
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
		if (getChildren().isEmpty())
			return getBindingClass() != null ? getBindingClass() : getDataClass();
		else
			return getChildren().get(getChildren().size() - 1).getPostInputClass();
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

	public static Method findUnbindingMethod(String propertyName,
			UnbindingStrategy unbindingStrategy, String unbindingMethodName,
			Class<?> unbindingClass, Class<?> dataClass) {
		if (unbindingStrategy == null || dataClass == null)
			return null;

		Method unbindingMethod;

		if (unbindingStrategy == UnbindingStrategy.SIMPLE
				|| unbindingStrategy == null) {
			if (unbindingClass != null || unbindingMethodName != null)
				throw new SchemaException();
			unbindingMethod = null;
		} else if (unbindingStrategy == UnbindingStrategy.CONSTRUCTOR) {
			if (unbindingMethodName != null)
				throw new SchemaException();
			unbindingMethod = null;
		} else {
			unbindingClass = unbindingClass != null ? unbindingClass : dataClass;

			try {
				Class<?> resultClass = (unbindingStrategy == UnbindingStrategy.STATIC_FACTORY) ? Object.class
						: null;

				List<String> names;
				if (unbindingMethodName != null)
					names = Arrays.asList(unbindingMethodName);
				else {
					names = BindingNodeConfigurator.generateOutMethodNames(propertyName,
							false, resultClass);
				}

				if (unbindingStrategy == UnbindingStrategy.ACCEPT_PROVIDED)
					unbindingMethod = BindingNodeConfigurator.findMethod(names,
							dataClass, resultClass, unbindingClass);
				else
					unbindingMethod = BindingNodeConfigurator.findMethod(names,
							unbindingClass, resultClass, dataClass);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new SchemaException(e);
			}
		}
		return unbindingMethod;
	}
}
