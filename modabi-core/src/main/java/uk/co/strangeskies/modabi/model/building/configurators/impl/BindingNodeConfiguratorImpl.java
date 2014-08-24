package uk.co.strangeskies.modabi.model.building.configurators.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.configurators.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.ChildrenConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.Methods;
import uk.co.strangeskies.modabi.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.model.building.impl.SequentialChildrenConfigurator;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.BindingNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;

public abstract class BindingNodeConfiguratorImpl<S extends BindingNodeConfigurator<S, N, T, C, B>, N extends BindingNode<T, N, ?>, T, C extends ChildNode<?, ?>, B extends BindingChildNode<?, ?, ?>>
		extends SchemaNodeConfiguratorImpl<S, N, C, B> implements
		BindingNodeConfigurator<S, N, T, C, B> {
	protected static abstract class BindingNodeImpl<T, S extends BindingNode<T, S, E>, E extends BindingNode.Effective<T, S, E>>
			extends SchemaNodeImpl<S, E> implements BindingNode<T, S, E> {
		protected static abstract class Effective<T, S extends BindingNode<T, S, E>, E extends BindingNode.Effective<T, S, E>>
				extends SchemaNodeImpl.Effective<S, E> implements
				BindingNode.Effective<T, S, E> {
			private final Boolean isAbstract;

			private final Class<T> dataClass;
			private final Class<?> bindingClass;
			private final Class<?> unbindingClass;
			private final Class<?> unbindingFactoryClass;
			private final BindingStrategy bindingStrategy;
			private final UnbindingStrategy unbindingStrategy;
			private final String unbindingMethodName;
			private final Method unbindingMethod;

			private final List<QualifiedName> providedUnbindingParameterNames;
			private final List<DataNode.Effective<?>> providedUnbindingParameters;

			protected Effective(
					OverrideMerge<S, ? extends BindingNodeConfiguratorImpl<?, ?, ?, ?, ?>> overrideMerge) {
				super(overrideMerge);

				isAbstract = overrideMerge.node().isAbstract() != null
						&& overrideMerge.node().isAbstract();

				dataClass = overrideMerge.getValue(BindingNode::getDataClass,
						(v, o) -> o.isAssignableFrom(v));

				bindingClass = overrideMerge.getValue(BindingNode::getBindingClass);

				unbindingClass = overrideMerge.getValue(BindingNode::getUnbindingClass);

				unbindingFactoryClass = overrideMerge
						.getValue(BindingNode::getUnbindingFactoryClass);

				bindingStrategy = overrideMerge
						.getValue(BindingNode::getBindingStrategy);

				unbindingStrategy = overrideMerge
						.getValue(BindingNode::getUnbindingStrategy);

				unbindingMethodName = overrideMerge.getValue(
						BindingNode::getUnbindingMethodName, Objects::equals);

				providedUnbindingParameterNames = overrideMerge.getValue(
						BindingNode::getProvidedUnbindingMethodParameterNames,
						Objects::equals);

				providedUnbindingParameters = Methods
						.findProvidedUnbindingParameters(this);

				unbindingMethod = Methods.findUnbindingMethod(this);

				// TODO verify unbinding method overrides okay...
			}

			@Override
			public Boolean isAbstract() {
				return isAbstract;
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

			@Override
			public List<DataNode.Effective<?>> getProvidedUnbindingMethodParameters() {
				return providedUnbindingParameters;
			}

			@Override
			public List<QualifiedName> getProvidedUnbindingMethodParameterNames() {
				return providedUnbindingParameterNames;
			}
		}

		private final Boolean isAbstract;

		private final Class<T> dataClass;
		private final Class<?> bindingClass;
		private final Class<?> unbindingClass;
		private final Class<?> unbindingFactoryClass;
		private final BindingStrategy bindingStrategy;
		private final UnbindingStrategy unbindingStrategy;
		private final String unbindingMethodName;

		private final List<QualifiedName> unbindingParameterNames;

		public BindingNodeImpl(
				BindingNodeConfiguratorImpl<?, ?, T, ?, ?> configurator) {
			super(configurator);

			isAbstract = configurator.isAbstract;

			dataClass = configurator.dataClass;

			bindingStrategy = configurator.bindingStrategy;
			bindingClass = configurator.bindingClass;

			unbindingStrategy = configurator.unbindingStrategy;
			unbindingClass = configurator.unbindingClass;
			unbindingMethodName = configurator.unbindingMethod;
			unbindingFactoryClass = configurator.unbindingFactoryClass;
			unbindingParameterNames = configurator.unbindingParameterNames == null ? null
					: Collections.unmodifiableList(new ArrayList<>(
							configurator.unbindingParameterNames));
		}

		@Override
		public Boolean isAbstract() {
			return isAbstract;
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

		@Override
		public List<QualifiedName> getProvidedUnbindingMethodParameterNames() {
			return unbindingParameterNames;
		}
	}

	private Boolean isAbstract;

	private Class<T> dataClass;

	private BindingStrategy bindingStrategy;
	private Class<?> bindingClass;

	private UnbindingStrategy unbindingStrategy;
	private Class<?> unbindingClass;
	private String unbindingMethod;

	private Class<?> unbindingFactoryClass;

	private List<QualifiedName> unbindingParameterNames;

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> BindingNodeConfigurator<?, ?, V, C, B> dataClass(
			Class<V> dataClass) {
		requireConfigurable(this.dataClass);
		this.dataClass = (Class<T>) dataClass;

		return (BindingNodeConfigurator<?, ?, V, C, B>) this;
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
	public ChildrenConfigurator<C, B> createChildrenConfigurator() {
		OverrideMerge<? extends BindingNode<?, ?, ?>, ?> overrideMerge = overrideMerge(
				null, this);

		Class<?> unbindingClass = overrideMerge.getValueWithOverride(
				this.unbindingClass, BindingNode::getUnbindingClass);
		Class<?> bindingClass = overrideMerge.getValueWithOverride(
				this.bindingClass, BindingNode::getBindingClass);
		Class<?> dataClass = overrideMerge.getValueWithOverride(this.dataClass,
				BindingNode::getDataClass);

		Class<?> inputTarget = bindingClass != null ? bindingClass : dataClass;
		Class<?> outputTarget = unbindingClass != null ? unbindingClass : dataClass;

		return new SequentialChildrenConfigurator<>(getNamespace(),
				getOverriddenNodes(), inputTarget, outputTarget, getDataLoader(),
				isChildContextAbstract());
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
	public final S isAbstract(boolean isAbstract) {
		requireConfigurable(this.isAbstract);
		this.isAbstract = isAbstract;

		return getThis();
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

	@Override
	public final S providedUnbindingParameters(List<QualifiedName> parameterNames) {
		requireConfigurable(unbindingParameterNames);
		unbindingParameterNames = new ArrayList<>(parameterNames);

		return getThis();
	}

	@Override
	public S providedUnbindingParameters(String... parameterNames) {
		return providedUnbindingParameters(Arrays.asList(parameterNames).stream()
				.map(n -> new QualifiedName(n, getName().getNamespace()))
				.collect(Collectors.toList()));
	}

	@Override
	public final ChildBuilder<C, B> addChild() {
		return super.addChild();
	}

	protected boolean isChildContextAbstract() {
		return isAbstract != null && isAbstract;
	}
}
