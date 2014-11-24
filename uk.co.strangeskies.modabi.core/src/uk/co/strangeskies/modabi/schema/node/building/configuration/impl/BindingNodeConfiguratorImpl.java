package uk.co.strangeskies.modabi.schema.node.building.configuration.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.management.binding.BindingStrategy;
import uk.co.strangeskies.modabi.schema.management.unbinding.UnbindingStrategy;
import uk.co.strangeskies.modabi.schema.node.BindingNode;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.modabi.schema.node.building.configuration.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.ChildrenConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.Methods;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.SequentialChildrenConfigurator;

public abstract class BindingNodeConfiguratorImpl<S extends BindingNodeConfigurator<S, N, T>, N extends BindingNode<T, N, ?>, T>
		extends SchemaNodeConfiguratorImpl<S, N> implements
		BindingNodeConfigurator<S, N, T> {
	protected static abstract class BindingNodeImpl<T, S extends BindingNode<T, S, E>, E extends BindingNode.Effective<T, S, E>>
			extends SchemaNodeImpl<S, E> implements BindingNode<T, S, E> {
		protected static abstract class Effective<T, S extends BindingNode<T, S, E>, E extends BindingNode.Effective<T, S, E>>
				extends SchemaNodeImpl.Effective<S, E> implements
				BindingNode.Effective<T, S, E> {
			private final Class<T> dataClass;
			private final Class<?> bindingClass;
			private final Class<?> unbindingClass;
			private final Class<?> unbindingFactoryClass;
			private final BindingStrategy bindingStrategy;
			private final UnbindingStrategy unbindingStrategy;
			private String unbindingMethodName;
			private final Method unbindingMethod;

			private final List<QualifiedName> providedUnbindingParameterNames;
			private final List<DataNode.Effective<?>> providedUnbindingParameters;

			protected Effective(
					OverrideMerge<S, ? extends BindingNodeConfiguratorImpl<?, S, ?>> overrideMerge) {
				super(overrideMerge);

				dataClass = overrideMerge.getValue(BindingNode::getDataType,
						(v, o) -> o.isAssignableFrom(v), null);

				bindingClass = overrideMerge.getValue(BindingNode::getBindingType, (v,
						o) -> o.isAssignableFrom(v), dataClass);

				unbindingClass = overrideMerge.getValue(BindingNode::getUnbindingType,
						(v, o) -> o.isAssignableFrom(v), dataClass);

				unbindingFactoryClass = overrideMerge.getValue(
						BindingNode::getUnbindingFactoryType,
						(v, o) -> o.isAssignableFrom(v), unbindingClass);

				bindingStrategy = overrideMerge.getValue(
						BindingNode::getBindingStrategy, BindingStrategy.PROVIDED);

				unbindingStrategy = overrideMerge.getValue(
						BindingNode::getUnbindingStrategy, UnbindingStrategy.SIMPLE);

				providedUnbindingParameterNames = overrideMerge.getValue(
						BindingNode::getProvidedUnbindingMethodParameterNames,
						Collections.emptyList());

				providedUnbindingParameters = isAbstract() ? null : Methods
						.findProvidedUnbindingParameters(this);

				unbindingMethodName = overrideMerge
						.tryGetValue(BindingNode::getUnbindingMethodName);

				unbindingMethod = isAbstract() ? null : findUnbindingMethod();

				if (unbindingMethodName == null && !isAbstract()
						&& unbindingStrategy != UnbindingStrategy.SIMPLE
						&& unbindingStrategy != UnbindingStrategy.CONSTRUCTOR)
					unbindingMethodName = unbindingMethod.getName();
			}

			@Override
			public Class<T> getDataType() {
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
			public Class<?> getBindingType() {
				return bindingClass;
			}

			@Override
			public Class<?> getUnbindingType() {
				return unbindingClass;
			}

			@Override
			public Class<?> getUnbindingFactoryType() {
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

			private Method findUnbindingMethod() {
				UnbindingStrategy unbindingStrategy = getUnbindingStrategy();
				if (unbindingStrategy == null)
					unbindingStrategy = UnbindingStrategy.SIMPLE;

				switch (unbindingStrategy) {
				case SIMPLE:
				case CONSTRUCTOR:
					return null;

				case STATIC_FACTORY:
				case PROVIDED_FACTORY:
					Class<?> receiverClass = getUnbindingFactoryType() != null ? getUnbindingFactoryType()
							: getUnbindingType();
					return findUnbindingMethod(getUnbindingType(), receiverClass,
							findUnbindingMethodParameterClasses(BindingNode::getDataType));

				case PASS_TO_PROVIDED:
					return findUnbindingMethod(null, getUnbindingType(),
							findUnbindingMethodParameterClasses(BindingNode::getDataType));

				case ACCEPT_PROVIDED:
					return findUnbindingMethod(
							null,
							getDataType(),
							findUnbindingMethodParameterClasses(BindingNode::getUnbindingType));
				}
				throw new AssertionError();
			}

			private List<Class<?>> findUnbindingMethodParameterClasses(
					Function<BindingNode.Effective<?, ?, ?>, Class<?>> nodeClass) {
				List<Class<?>> classList = new ArrayList<>();

				boolean addedNodeClass = false;
				List<DataNode.Effective<?>> parameters = getProvidedUnbindingMethodParameters();
				if (parameters != null) {
					for (DataNode.Effective<?> parameter : parameters) {
						if (parameter == null)
							if (addedNodeClass)
								throw new SchemaException();
							else {
								addedNodeClass = true;
								classList.add(nodeClass.apply(this));
							}
						else {
							classList.add(parameter.getDataType());
						}
					}
				}
				if (!addedNodeClass)
					classList.add(0, nodeClass.apply(this));

				return classList;
			}

			private Method findUnbindingMethod(Class<?> result, Class<?> receiver,
					List<Class<?>> parameters) {
				List<String> names = generateUnbindingMethodNames(result);
				try {
					return Methods.findMethod(names, receiver,
							getBindingStrategy() == BindingStrategy.STATIC_FACTORY, result,
							false, parameters);
				} catch (NoSuchMethodException | SchemaException | SecurityException e) {
					throw new SchemaException("Cannot find unbinding method for node '"
							+ this + "' of class '" + result + "', reveiver '" + receiver
							+ "', and parameters '" + parameters + "' with any name of '"
							+ names + "'.", e);
				}
			}

			private List<String> generateUnbindingMethodNames(Class<?> resultClass) {
				List<String> names;

				if (getUnbindingMethodName() != null)
					names = Arrays.asList(getUnbindingMethodName());
				else
					names = generateUnbindingMethodNames(getName().getName(), false,
							resultClass);

				return names;
			}

			protected static List<String> generateUnbindingMethodNames(
					String propertyName, boolean isIterable, Class<?> resultClass) {
				List<String> names = new ArrayList<>();

				names.add(propertyName);
				names.add(propertyName + "Value");
				if (isIterable) {
					for (String name : new ArrayList<>(names)) {
						names.add(name + "s");
						names.add(name + "List");
						names.add(name + "Set");
						names.add(name + "Collection");
						names.add(name + "Array");
					}
				}
				if (resultClass != null
						&& (resultClass.equals(Boolean.class) || resultClass
								.equals(boolean.class)))
					names.add("is"
							+ InputNodeConfigurationHelper.capitalize(propertyName));

				List<String> namesAndBlank = new ArrayList<>(names);
				namesAndBlank.add("");

				for (String name : namesAndBlank) {
					names.add("get" + InputNodeConfigurationHelper.capitalize(name));
					names.add("to" + InputNodeConfigurationHelper.capitalize(name));
					names.add("compose" + InputNodeConfigurationHelper.capitalize(name));
					names.add("create" + InputNodeConfigurationHelper.capitalize(name));
				}

				return names;
			}
		}

		private final Class<T> dataClass;
		private final Class<?> bindingClass;
		private final Class<?> unbindingClass;
		private final Class<?> unbindingFactoryClass;
		private final BindingStrategy bindingStrategy;
		private final UnbindingStrategy unbindingStrategy;
		private final String unbindingMethodName;

		private final List<QualifiedName> unbindingParameterNames;

		public BindingNodeImpl(BindingNodeConfiguratorImpl<?, ?, T> configurator) {
			super(configurator);

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
		public Class<T> getDataType() {
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
		public Class<?> getBindingType() {
			return bindingClass;
		}

		@Override
		public Class<?> getUnbindingType() {
			return unbindingClass;
		}

		@Override
		public Class<?> getUnbindingFactoryType() {
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
	public <V extends T> BindingNodeConfigurator<?, ?, V> dataClass(
			Class<V> dataClass) {
		assertConfigurable(this.dataClass);
		this.dataClass = (Class<T>) dataClass;

		return (BindingNodeConfigurator<?, ?, V>) this;
	}

	@Override
	public ChildrenConfigurator createChildrenConfigurator() {
		OverrideMerge<? extends BindingNode<?, ?, ?>, ? extends BindingNodeConfigurator<?, ?, ?>> overrideMerge = overrideMerge(
				null, this);

		Class<?> unbindingClass = overrideMerge.getValueWithOverride(
				this.unbindingClass, BindingNode::getUnbindingType,
				(o, n) -> n.isAssignableFrom(o));
		Class<?> bindingClass = overrideMerge.getValueWithOverride(
				this.bindingClass, BindingNode::getBindingType,
				(o, n) -> n.isAssignableFrom(o));
		Class<?> dataClass = overrideMerge.getValueWithOverride(this.dataClass,
				BindingNode::getDataType, (o, n) -> n.isAssignableFrom(o));

		Class<?> inputTarget = bindingClass != null ? bindingClass : dataClass;
		Class<?> outputTarget = unbindingClass != null ? unbindingClass : dataClass;

		/*
		 * TODO make 'hasInput' optional for IMPLEMENT_IN_PLACE
		 */
		return new SequentialChildrenConfigurator(
				new SchemaNodeConfigurationContext<ChildNode<?, ?>>() {
					@Override
					public DataLoader dataLoader() {
						return getDataLoader();
					}

					@Override
					public boolean isAbstract() {
						return isChildContextAbstract();
					}

					@Override
					public boolean isInputExpected() {
						return true;
					}

					@Override
					public boolean isInputDataOnly() {
						return isDataContext();
					}

					@Override
					public boolean isConstructorExpected() {
						return bindingStrategy == BindingStrategy.CONSTRUCTOR;
					}

					public boolean isStaticMethodExpected() {
						return bindingStrategy == BindingStrategy.STATIC_FACTORY;
					}

					@Override
					public Namespace namespace() {
						return getNamespace();
					}

					@Override
					public Class<?> inputTargetClass(QualifiedName node) {
						return inputTarget;
					}

					@Override
					public Class<?> outputSourceClass() {
						return outputTarget;
					}

					@Override
					public void addChild(ChildNode<?, ?> result) {
					}

					@Override
					public <U extends ChildNode<?, ?>> List<U> overrideChild(
							QualifiedName id, Class<U> nodeClass) {
						return null;
					}

					@Override
					public List<? extends SchemaNode<?, ?>> overriddenNodes() {
						return getOverriddenNodes();
					}
				});
	}

	protected abstract boolean isDataContext();

	@Override
	public final S bindingClass(Class<?> bindingClass) {
		assertConfigurable(this.bindingClass);
		this.bindingClass = bindingClass;

		return getThis();
	}

	@Override
	public S unbindingClass(Class<?> unbindingClass) {
		assertConfigurable(this.unbindingClass);
		this.unbindingClass = unbindingClass;

		return getThis();
	}

	@Override
	public S unbindingMethod(String unbindingMethod) {
		assertConfigurable(this.unbindingMethod);
		this.unbindingMethod = unbindingMethod;

		return getThis();
	}

	@Override
	public final S bindingStrategy(BindingStrategy strategy) {
		assertConfigurable(bindingStrategy);
		bindingStrategy = strategy;

		return getThis();
	}

	@Override
	public final S unbindingStrategy(UnbindingStrategy strategy) {
		assertConfigurable(unbindingStrategy);
		unbindingStrategy = strategy;

		return getThis();
	}

	@Override
	public S unbindingFactoryClass(Class<?> factoryClass) {
		assertConfigurable(unbindingFactoryClass);
		unbindingFactoryClass = factoryClass;

		return getThis();
	}

	@Override
	public final S providedUnbindingMethodParameters(
			List<QualifiedName> parameterNames) {
		assertConfigurable(unbindingParameterNames);
		unbindingParameterNames = new ArrayList<>(parameterNames);

		return getThis();
	}

	@Override
	public S providedUnbindingMethodParameters(String... parameterNames) {
		return providedUnbindingMethodParameters(Arrays.asList(parameterNames)
				.stream().map(n -> new QualifiedName(n, getName().getNamespace()))
				.collect(Collectors.toList()));
	}
}
