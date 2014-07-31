package uk.co.strangeskies.modabi.data.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.DataBindingTypeConfigurator;
import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.DataLoader;
import uk.co.strangeskies.modabi.model.building.configurators.impl.BindingNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.building.configurators.impl.OverrideMerge;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.BindingNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.DataNodeChildNode;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;

public class DataBindingTypeConfiguratorImpl<T>
		extends
		BindingNodeConfiguratorImpl<DataBindingTypeConfigurator<T>, DataBindingType<T>, T, ChildNode<?>, BindingChildNode<?, ?>>
		implements DataBindingTypeConfigurator<T> {
	public static class DataBindingTypeImpl<T> implements DataBindingType<T> {
		private static class Effective<T> implements DataBindingType.Effective<T> {
			private final String name;
			private final Class<T> dataClass;

			private final BindingStrategy bindingStrategy;
			private final Class<?> bindingClass;

			private final UnbindingStrategy unbindingStrategy;
			private final Class<?> unbindingClass;
			private final String unbindingMethodName;
			private final Method unbindingMethod;
			private final Class<?> unbindingFactoryClass;

			private final Boolean isAbstract;
			private final Boolean isPrivate;

			private final List<ChildNode.Effective<?>> children;

			private final List<DataNode.Effective<?>> providedUnbindingParameters;

			private final DataBindingType.Effective<? super T> baseType;

			public Effective(
					OverrideMerge<DataBindingType<T>, DataBindingTypeConfiguratorImpl<T>> overrideMerge) {
				name = overrideMerge.node().getName();

				dataClass = overrideMerge.getValue(n -> n.getDataClass(),
						(v, o) -> o.isAssignableFrom(v));

				bindingClass = overrideMerge.getValue(n -> n.getBindingClass(),
						(v, o) -> o.isAssignableFrom(v));

				unbindingClass = overrideMerge.getValue(n -> n.getUnbindingClass(), (v,
						o) -> o.isAssignableFrom(v));

				unbindingFactoryClass = overrideMerge.getValue(n -> n
						.getUnbindingFactoryClass());

				bindingStrategy = overrideMerge.getValue(n -> n.getBindingStrategy(),
						Objects::equals);

				unbindingStrategy = overrideMerge.getValue(
						n -> n.getUnbindingStrategy(), Objects::equals);

				unbindingMethodName = overrideMerge.getValue(
						n -> n.getUnbindingMethodName(), Objects::equals);

				isAbstract = overrideMerge.getValue(n -> n.isAbstract());
				isPrivate = overrideMerge.getValue(n -> n.isPrivate());

				children = overrideMerge.configurator().children.getEffectiveChildren();

				providedUnbindingParameters = overrideMerge.configurator().unbindingParameterNames
						.stream()
						.map(
								p -> {
									if (p.equals("this"))
										return null;
									else {
										ChildNode.Effective<?> node = children
												.stream()
												.filter(c -> c.getId().equals(p))
												.findAny()
												.orElseThrow(
														() -> new SchemaException(
																"Cannot find node for unbinding parameter: '"
																		+ p + "'"));

										if (!(node instanceof DataNode.Effective))
											throw new SchemaException("Unbinding parameter node '"
													+ node + "' for '" + p + "' is not a data node.");

										DataNode.Effective<?> dataNode = (DataNode.Effective<?>) node;

										if (!dataNode.isValueProvided())
											throw new SchemaException("Unbinding parameter node '"
													+ node + "' for '" + p + "' must provide a value.");

										return dataNode;
									}
								}).collect(Collectors.toList());

				baseType = overrideMerge.configurator().baseType.effective();

				unbindingMethod = BindingNode.Effective.findUnbindingMethod(this);
			}

			@Override
			public boolean equals(Object obj) {
				if (!(obj instanceof Effective))
					return false;
				return super.equals(obj);
			}

			@Override
			public List<String> providedUnbindingMethodParameterNames() {
				return providedUnbindingParameters.stream().map(n -> n.getId())
						.collect(Collectors.toList());
			}

			@Override
			public List<DataNode.Effective<?>> providedUnbindingMethodParameters() {
				return providedUnbindingParameters;
			}

			@Override
			public final String getName() {
				return name;
			}

			@Override
			public final Class<T> getDataClass() {
				return dataClass;
			}

			@Override
			public final Class<?> getBindingClass() {
				return bindingClass;
			}

			@Override
			public Boolean isAbstract() {
				return isAbstract;
			}

			@Override
			public Boolean isPrivate() {
				return isPrivate;
			}

			@Override
			public final List<ChildNode.Effective<?>> children() {
				return children;
			}

			@Override
			public BindingStrategy getBindingStrategy() {
				return bindingStrategy;
			}

			@Override
			public Class<?> getUnbindingClass() {
				return unbindingClass;
			}

			@Override
			public UnbindingStrategy getUnbindingStrategy() {
				return unbindingStrategy;
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
			public Class<?> getUnbindingFactoryClass() {
				return unbindingFactoryClass;
			}

			@Override
			public DataBindingType.Effective<? super T> baseType() {
				return baseType;
			}
		}

		private final Effective<T> effective;

		private final String name;
		private final Class<T> dataClass;

		private final BindingStrategy bindingStrategy;
		private final Class<?> bindingClass;

		private final UnbindingStrategy unbindingStrategy;
		private final Class<?> unbindingClass;
		private final String unbindingMethodName;
		private final Class<?> unbindingFactoryClass;

		private final Boolean isAbstract;
		private final Boolean isPrivate;

		private final List<ChildNode<?>> children;

		private final List<String> providedUnbindingParameters;

		private final DataBindingType<? super T> baseType;

		@SuppressWarnings("unchecked")
		public DataBindingTypeImpl(DataBindingTypeConfiguratorImpl<T> configurator) {
			name = configurator.name;
			dataClass = configurator.dataClass;

			bindingStrategy = configurator.bindingStrategy;
			bindingClass = configurator.bindingClass;

			unbindingStrategy = configurator.unbindingStrategy;
			unbindingClass = configurator.unbindingClass;
			unbindingFactoryClass = configurator.unbindingFactoryClass;

			unbindingMethodName = configurator.unbindingMethodName;

			isAbstract = configurator.isAbstract;
			isPrivate = configurator.isPrivate;

			children = Collections.unmodifiableList(new ArrayList<>(
					configurator.children.getChildren()));

			providedUnbindingParameters = Collections
					.unmodifiableList(new ArrayList<>(
							configurator.unbindingParameterNames));

			baseType = configurator.baseType;

			effective = new Effective<>(new OverrideMerge<>(this, configurator,
					c -> c.baseType != null ? Arrays
							.asList((DataBindingType<T>) c.baseType) : Collections
							.emptyList()));
		}

		@Override
		public List<String> providedUnbindingMethodParameterNames() {
			return providedUnbindingParameters;
		}

		@Override
		public final String getName() {
			return name;
		}

		@Override
		public final Class<T> getDataClass() {
			return dataClass;
		}

		@Override
		public final Class<?> getBindingClass() {
			return bindingClass;
		}

		@Override
		public Boolean isAbstract() {
			return isAbstract;
		}

		@Override
		public Boolean isPrivate() {
			return isPrivate;
		}

		@Override
		public final List<ChildNode<?>> children() {
			return children;
		}

		@Override
		public BindingStrategy getBindingStrategy() {
			return bindingStrategy;
		}

		@Override
		public Class<?> getUnbindingClass() {
			return unbindingClass;
		}

		@Override
		public UnbindingStrategy getUnbindingStrategy() {
			return unbindingStrategy;
		}

		@Override
		public String getUnbindingMethodName() {
			return unbindingMethodName;
		}

		@Override
		public Class<?> getUnbindingFactoryClass() {
			return unbindingFactoryClass;
		}

		@Override
		public DataBindingType<? super T> baseType() {
			return baseType;
		}

		@Override
		public Effective<T> effective() {
			return effective;
		}
	}

	private final DataLoader loader;

	private String name;
	private Class<T> dataClass;

	private BindingStrategy bindingStrategy;
	private Class<?> bindingClass;

	private UnbindingStrategy unbindingStrategy;
	private Class<?> unbindingClass;
	private Class<?> unbindingFactoryClass;
	private String unbindingMethodName;

	private Boolean isAbstract;
	private Boolean isPrivate;

	private DataBindingType<? super T> baseType;

	private ArrayList<String> unbindingParameterNames;

	public DataBindingTypeConfiguratorImpl(DataLoader loader) {
		this.loader = loader;
	}

	@Override
	protected DataBindingType<T> tryCreate() {
		return new DataBindingTypeImpl<>(this);
	}

	@Override
	public DataBindingTypeConfigurator<T> name(String name) {
		requireConfigurable(this.name);
		this.name = name;

		return this;
	}

	@Override
	public ChildBuilder<DataNodeChildNode<?>, DataNode<?>> addChild() {
		children.assertUnblocked();
		finaliseProperties();

		return children.addChild(loader, getCurrentChildInputTargetClass(),
				getCurrentChildOutputTargetClass());
	}

	@Override
	public DataBindingTypeConfigurator<T> bindingStrategy(BindingStrategy strategy) {
		requireConfigurable(bindingStrategy);
		bindingStrategy = strategy;

		return this;
	}

	@Override
	public DataBindingTypeConfigurator<T> unbindingStrategy(
			UnbindingStrategy strategy) {
		requireConfigurable(unbindingStrategy);
		unbindingStrategy = strategy;

		return this;
	}

	@Override
	public DataBindingTypeConfigurator<T> unbindingClass(Class<?> unbindingClass) {
		requireConfigurable(this.unbindingClass);
		this.unbindingClass = unbindingClass;

		return this;
	}

	@Override
	public DataBindingTypeConfigurator<T> unbindingFactroyClass(
			Class<?> factoryClass) {
		requireConfigurable(this.unbindingFactoryClass);
		this.unbindingFactoryClass = factoryClass;

		return this;
	}

	@Override
	public DataBindingTypeConfigurator<T> isAbstract(boolean isAbstract) {
		requireConfigurable(this.isAbstract);
		this.isAbstract = isAbstract;

		return this;
	}

	@Override
	public DataBindingTypeConfigurator<T> isPrivate(boolean isPrivate) {
		requireConfigurable(this.isPrivate);
		this.isPrivate = isPrivate;

		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <U extends T> DataBindingTypeConfigurator<U> baseType(
			DataBindingType<? super U> baseType) {
		requireConfigurable(this.baseType);
		this.baseType = (DataBindingType<? super T>) baseType;

		children.inheritChildren(baseType.effective().children());

		return (DataBindingTypeConfigurator<U>) this;
	}

	@Override
	public DataBindingTypeConfigurator<T> providedUnbindingParameters(
			List<String> parameterNames) {
		requireConfigurable(unbindingParameterNames);
		unbindingParameterNames = new ArrayList<>(parameterNames);

		return this;
	}
}
