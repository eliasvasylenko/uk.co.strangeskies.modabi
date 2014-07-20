package uk.co.strangeskies.modabi.data.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.co.strangeskies.gears.utilities.collection.HashSetMultiHashMap;
import uk.co.strangeskies.gears.utilities.collection.SetMultiMap;
import uk.co.strangeskies.gears.utilities.factory.Configurator;
import uk.co.strangeskies.gears.utilities.factory.InvalidBuildStateException;
import uk.co.strangeskies.modabi.data.AbstractDataBindingType;
import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.DataBindingTypeConfigurator;
import uk.co.strangeskies.modabi.data.EffectiveDataBindingType;
import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.DataLoader;
import uk.co.strangeskies.modabi.model.building.configurators.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.DataNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.model.building.impl.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.model.building.impl.configurators.BindingNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.building.impl.configurators.ChoiceNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.building.impl.configurators.DataNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.building.impl.configurators.InputSequenceNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.building.impl.configurators.SequenceNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.DataNodeChildNode;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;

public class DataBindingTypeConfiguratorImpl<T> extends
		Configurator<DataBindingType<T>> implements DataBindingTypeConfigurator<T> {
	public static class AbstractDataBindingTypeImpl<T> implements
			AbstractDataBindingType<T> {
		private final String name;
		private final Class<T> dataClass;

		private final BindingStrategy bindingStrategy;
		private final Class<?> bindingClass;

		private final UnbindingStrategy unbindingStrategy;
		private final Class<?> unbindingClass;
		private final String unbindingMethodName;
		private final Method unbindingMethod;

		private final Boolean isAbstract;
		private final Boolean isPrivate;

		private final List<ChildNode> children;

		private final DataBindingType<? super T> baseType;

		public AbstractDataBindingTypeImpl(
				DataBindingTypeConfiguratorImpl<T> configurator) {
			name = configurator.name;
			dataClass = configurator.dataClass;

			bindingStrategy = configurator.bindingStrategy;
			bindingClass = configurator.bindingClass;

			unbindingStrategy = configurator.unbindingStrategy;
			unbindingClass = configurator.unbindingClass;

			unbindingMethodName = configurator.unbindingMethodName;
			unbindingMethod = BindingNodeConfiguratorImpl.findUnbindingMethod(name,
					getUnbindingStrategy(), getUnbindingMethodName(),
					getUnbindingClass(), getDataClass());

			isAbstract = configurator.isAbstract;
			isPrivate = configurator.isPrivate;

			children = Collections.unmodifiableList(new ArrayList<>(
					configurator.children));

			baseType = configurator.baseType;
		}

		@SuppressWarnings("unchecked")
		public AbstractDataBindingTypeImpl(AbstractDataBindingType<T> node,
				AbstractDataBindingType<? super T> overriddenType,
				List<ChildNode> effectiveChildren) {
			OverrideMerge<AbstractDataBindingType<? super T>> overrideMerge = new OverrideMerge<>(
					node, overriddenType == null ? Collections.emptyList()
							: Arrays.asList(overriddenType));
			name = node.getName();

			dataClass = (Class<T>) overrideMerge.getValue(n -> n.getDataClass(), (v,
					o) -> o.isAssignableFrom(v));

			bindingClass = overrideMerge.getValue(n -> n.getBindingClass());

			unbindingClass = overrideMerge.getValue(n -> n.getUnbindingClass());

			bindingStrategy = overrideMerge.getValue(n -> n.getBindingStrategy());

			unbindingStrategy = overrideMerge.getValue(n -> n.getUnbindingStrategy());

			unbindingMethodName = overrideMerge.getValue(
					n -> n.getUnbindingMethodName(), (o, v) -> o.equals(v));

			unbindingMethod = overrideMerge.getValue(n -> n.getUnbindingMethod());

			isAbstract = overrideMerge.getValue(n -> n.isAbstract());
			isPrivate = overrideMerge.getValue(n -> n.isPrivate());

			children = effectiveChildren;

			baseType = overrideMerge.getValue(n -> (DataBindingType<T>) n.baseType(),
					(n, o) -> {
						DataBindingType<?> p = n;
						do
							if (p == o)
								return true;
						while ((p = p.baseType()) != null);
						return false;
					});
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
		public final List<ChildNode> getChildren() {
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
		public DataBindingType<? super T> baseType() {
			return baseType;
		}
	}

	protected static class EffectiveDataBindingTypeImpl<T> extends
			AbstractDataBindingTypeImpl<T> implements EffectiveDataBindingType<T> {
		public EffectiveDataBindingTypeImpl(DataBindingTypeImpl<T> node,
				EffectiveDataBindingType<? super T> overriddenType,
				List<ChildNode> effectiveChildren) {
			super(node, overriddenType, effectiveChildren);
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof EffectiveDataBindingTypeImpl))
				return false;
			return super.equals(obj);
		}
	}

	protected static class DataBindingTypeImpl<T> extends
			AbstractDataBindingTypeImpl<T> implements DataBindingType<T> {
		private final EffectiveDataBindingType<T> effectiveType;

		public DataBindingTypeImpl(DataBindingTypeConfiguratorImpl<T> configurator) {
			super(configurator);

			effectiveType = new EffectiveDataBindingTypeImpl<T>(this,
					baseType() == null ? null : baseType().effectiveType(),
					configurator.getEffectiveChildren());
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataBindingTypeImpl))
				return false;
			return super.equals(obj);
		}

		@Override
		public EffectiveDataBindingType<T> effectiveType() {
			return effectiveType;
		}
	}

	private final DataLoader loader;

	private String name;
	private Class<T> dataClass;

	private BindingStrategy bindingStrategy;
	private Class<?> bindingClass;

	private UnbindingStrategy unbindingStrategy;
	private Class<?> unbindingClass;

	private String unbindingMethodName;

	private Boolean isAbstract;
	private Boolean isPrivate;

	private final List<ChildNode> children;
	private final List<ChildNode> effectiveChildren;
	private final SetMultiMap<String, ChildNode> namedInheritedChildren;
	private final List<ChildNode> inheritedChildren;

	private boolean finalisedProperties;
	private boolean blocked;
	private DataBindingType<? super T> baseType;

	public DataBindingTypeConfiguratorImpl(DataLoader loader) {
		this.loader = loader;

		children = new ArrayList<>();
		effectiveChildren = new ArrayList<>();
		inheritedChildren = new ArrayList<>();
		namedInheritedChildren = new HashSetMultiHashMap<>();

		finalisedProperties = false;
	}

	@Override
	protected DataBindingType<T> tryCreate() {
		return new DataBindingTypeImpl<>(this);
	}

	protected final void requireConfigurable(Object object) {
		requireConfigurable();
		if (object != null)
			throw new InvalidBuildStateException(this);
	}

	protected final void requireConfigurable() {
		if (finalisedProperties)
			throw new InvalidBuildStateException(this);
	}

	protected void finaliseProperties() {
		finalisedProperties = true;
	}

	@Override
	public DataBindingTypeConfigurator<T> name(String name) {
		requireConfigurable(this.name);
		this.name = name;

		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U extends T> DataBindingTypeConfigurator<U> dataClass(
			Class<U> dataClass) {
		requireConfigurable(this.dataClass);
		this.dataClass = (Class<T>) dataClass;

		return (DataBindingTypeConfigurator<U>) this;
	}

	@Override
	public DataBindingTypeConfigurator<T> bindingClass(Class<?> bindingClass) {
		requireConfigurable(this.bindingClass);
		this.bindingClass = bindingClass;

		return this;
	}

	@Override
	public DataBindingTypeConfigurator<T> unbindingMethod(String name) {
		requireConfigurable(unbindingMethodName);
		unbindingMethodName = name;

		return this;
	}

	protected final Class<?> getCurrentChildOutputTargetClass() {
		if (unbindingStrategy == null
				|| unbindingStrategy == UnbindingStrategy.SIMPLE)
			return dataClass;
		return unbindingClass != null ? unbindingClass : dataClass;
	}

	protected Class<?> getCurrentChildInputTargetClass() {
		if (children.isEmpty())
			return bindingClass != null ? bindingClass : dataClass;
		else
			return children.get(children.size() - 1).getPostInputClass();
	}

	protected void assertUnblocked() {
		if (blocked)
			throw new InvalidBuildStateException(this);
	}

	void addChild(ChildNode result, ChildNode effective) {
		blocked = false;
		children.add(result);
		effectiveChildren.add(effective);
	}

	@Override
	public ChildBuilder<DataNodeChildNode, DataNode<?>> addChild() {
		assertUnblocked();
		blocked = true;

		SchemaNodeConfigurationContext<ChildNode> context = new SchemaNodeConfigurationContext<ChildNode>() {
			@Override
			public DataLoader getDataLoader() {
				return loader;
			}

			@Override
			public <U extends ChildNode> Set<U> overrideChild(String id,
					Class<U> nodeClass) {
				return DataBindingTypeConfiguratorImpl.this
						.overrideChild(id, nodeClass);
			}

			@Override
			public Class<?> getCurrentChildOutputTargetClass() {
				return DataBindingTypeConfiguratorImpl.this
						.getCurrentChildOutputTargetClass();
			}

			@Override
			public Class<?> getCurrentChildInputTargetClass() {
				return DataBindingTypeConfiguratorImpl.this
						.getCurrentChildInputTargetClass();
			}

			@Override
			public void addChild(ChildNode result, ChildNode effective) {
				DataBindingTypeConfiguratorImpl.this.addChild(result, effective);
			}
		};

		return new ChildBuilder<DataNodeChildNode, DataNode<?>>() {
			@Override
			public InputSequenceNodeConfigurator<DataNode<?>> inputSequence() {
				return new InputSequenceNodeConfiguratorImpl<>(context);
			}

			@Override
			public DataNodeConfigurator<Object> data() {
				return new DataNodeConfiguratorImpl<Object>(context);
			}

			@Override
			public ChoiceNodeConfigurator<DataNodeChildNode, DataNode<?>> choice() {
				return new ChoiceNodeConfiguratorImpl<>(context);
			}

			@Override
			public SequenceNodeConfigurator<DataNodeChildNode, DataNode<?>> sequence() {
				return new SequenceNodeConfiguratorImpl<>(context);
			}

			@Override
			public ElementNodeConfigurator<Object> element() {
				throw new UnsupportedOperationException();
			}
		};
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

		inheritChildren(baseType.effectiveType().getChildren());

		return (DataBindingTypeConfigurator<U>) this;
	}

	protected void inheritChildren(List<? extends ChildNode> nodes) {
		inheritChildren(inheritedChildren.size(), nodes);
	}

	protected void inheritChildren(int index, List<? extends ChildNode> nodes) {
		requireConfigurable();
		inheritNamedChildren(nodes);
		inheritedChildren.addAll(index, nodes);
	}

	private void inheritNamedChildren(List<? extends ChildNode> nodes) {
		nodes.stream().filter(c -> c.getId() != null)
				.forEach(c -> namedInheritedChildren.add(c.getId(), c));
	}

	@SuppressWarnings("unchecked")
	<U extends ChildNode> Set<U> overrideChild(String id, Class<U> nodeClass) {
		Set<ChildNode> overriddenNodes = namedInheritedChildren.get(id);

		if (overriddenNodes != null) {
			if (overriddenNodes.stream().anyMatch(
					n -> !nodeClass.isAssignableFrom(n.getClass())))
				throw new InvalidBuildStateException(this);
		} else
			overriddenNodes = new HashSet<>();

		return (Set<U>) Collections.unmodifiableSet(overriddenNodes);
	}

	protected final List<ChildNode> getEffectiveChildren() {
		List<ChildNode> effectiveChildren = new ArrayList<>();
		effectiveChildren.addAll(inheritedChildren);
		effectiveChildren.addAll(this.effectiveChildren);
		return effectiveChildren;
	}
}
