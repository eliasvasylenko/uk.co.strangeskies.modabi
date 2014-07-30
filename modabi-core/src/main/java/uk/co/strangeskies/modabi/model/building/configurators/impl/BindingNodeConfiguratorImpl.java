package uk.co.strangeskies.modabi.model.building.configurators.impl;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import uk.co.strangeskies.modabi.model.building.configurators.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.BindingNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;

public abstract class BindingNodeConfiguratorImpl<S extends BindingNodeConfigurator<S, N, T>, N extends BindingNode<T, ?>, T, C extends ChildNode<?>, B extends BindingChildNode<?, ?>>
		extends SchemaNodeConfiguratorImpl<S, N, C, B> implements
		BindingNodeConfigurator<S, N, T> {
	protected static abstract class BindingNodeImpl<T, E extends BindingNode.Effective<T, E>>
			extends SchemaNodeImpl<E> implements BindingNode<T, E> {
		protected static abstract class Effective<T, E extends BindingNode.Effective<T, E>>
				extends SchemaNodeImpl.Effective<E> implements
				BindingNode.Effective<T, E> {
			private final Class<T> dataClass;
			private final Class<?> bindingClass;
			private final Class<?> unbindingClass;
			private final Class<?> unbindingFactoryClass;
			private final BindingStrategy bindingStrategy;
			private final UnbindingStrategy unbindingStrategy;
			private final String unbindingMethodName;
			private final Method unbindingMethod;

			@SuppressWarnings("unchecked")
			protected Effective(
					OverrideMerge<? extends BindingNode<?, ?>, ? extends BindingNodeConfiguratorImpl<?, ?, ?, ?, ?>> overrideMerge) {
				super(overrideMerge);

				dataClass = (Class<T>) overrideMerge.getValue(n -> n.getDataClass(), (
						v, o) -> o.isAssignableFrom(v));

				bindingClass = overrideMerge.getValue(n -> n.getBindingClass());

				unbindingClass = overrideMerge.getValue(n -> n.getUnbindingClass());

				unbindingFactoryClass = overrideMerge.getValue(n -> n
						.getUnbindingFactoryClass());

				bindingStrategy = overrideMerge.getValue(n -> n.getBindingStrategy());

				unbindingStrategy = overrideMerge.getValue(n -> n
						.getUnbindingStrategy());

				unbindingMethodName = overrideMerge.getValue(
						n -> n.getUnbindingMethodName(), (o, v) -> o.equals(v));

				unbindingMethod = BindingNode.Effective.findUnbindingMethod(this);
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
			public Class<?> getUnbindingFactoryClass() {
				return unbindingFactoryClass;
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

		private final Class<T> dataClass;
		private final Class<?> bindingClass;
		private final Class<?> unbindingClass;
		private final Class<?> unbindingFactoryClass;
		private final BindingStrategy bindingStrategy;
		private final UnbindingStrategy unbindingStrategy;
		private final String unbindingMethodName;

		public BindingNodeImpl(
				BindingNodeConfiguratorImpl<?, ?, T, ?, ?> configurator) {
			super(configurator);

			dataClass = configurator.dataClass;

			bindingStrategy = configurator.bindingStrategy;
			bindingClass = configurator.bindingClass;

			unbindingStrategy = configurator.unbindingStrategy;
			unbindingClass = configurator.unbindingClass;
			unbindingMethodName = configurator.unbindingMethod;
			unbindingFactoryClass = configurator.unbindingFactoryClass;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof BindingNode))
				return false;

			BindingNode<?, ?> other = (BindingNode<?, ?>) obj;
			return super.equals(obj)
					&& Objects.equals(dataClass, other.getDataClass())
					&& Objects.equals(bindingClass, other.getBindingClass())
					&& Objects.equals(unbindingClass, other.getUnbindingClass())
					&& Objects.equals(bindingStrategy, other.getBindingStrategy())
					&& Objects.equals(unbindingStrategy, other.getUnbindingStrategy())
					&& Objects
							.equals(unbindingMethodName, other.getUnbindingMethodName());
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
		public Class<?> getUnbindingFactoryClass() {
			return unbindingFactoryClass;
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

	private Class<?> unbindingFactoryClass;

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
					.get(getChildren().getChildren().size() - 1).effective()
					.getPostInputClass();
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
	public S unbindingFactoryClass(Class<?> factoryClass) {
		requireConfigurable(unbindingFactoryClass);
		unbindingFactoryClass = factoryClass;

		return getThis();
	}

	public static List<String> getNames(String propertyName,
			String unbindingMethodName, Class<?> resultClass) {
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
