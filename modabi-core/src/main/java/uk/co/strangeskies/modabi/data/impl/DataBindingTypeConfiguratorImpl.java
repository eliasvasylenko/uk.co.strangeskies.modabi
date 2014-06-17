package uk.co.strangeskies.modabi.data.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import uk.co.strangeskies.gears.utilities.factory.Configurator;
import uk.co.strangeskies.gears.utilities.factory.InvalidBuildStateException;
import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.DataBindingTypeConfigurator;
import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.DataNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.BindingNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.building.impl.ChoiceNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.building.impl.DataNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.building.impl.InputSequenceNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.building.impl.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.model.building.impl.SequenceNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.DataNodeChildNode;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;

public class DataBindingTypeConfiguratorImpl<T> extends
		Configurator<DataBindingType<T>> implements DataBindingTypeConfigurator<T> {
	public static class DataTypeImpl<T> implements DataBindingType<T> {
		private final String name;
		private final Class<T> dataClass;

		private final BindingStrategy bindingStrategy;
		private final Class<?> bindingClass;

		private final UnbindingStrategy unbindingStrategy;
		private final Class<?> unbindingClass;
		private final String unbindingMethodName;
		private final Method unbindingMethod;

		private final boolean hidden;

		private final List<ChildNode> children;
		private final List<ChildNode> effectiveChildren;

		public DataTypeImpl(DataBindingTypeConfiguratorImpl<T> configurator) {
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

			hidden = configurator.hidden;

			children = Collections.unmodifiableList(new ArrayList<>(
					configurator.children));
			effectiveChildren = Collections.unmodifiableList(new ArrayList<>(
					configurator.effectiveChildren));
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
		public boolean isHidden() {
			return hidden;
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
	}

	private String name;
	private Class<T> dataClass;

	private BindingStrategy bindingStrategy;
	private Class<?> bindingClass;

	private UnbindingStrategy unbindingStrategy;
	private Class<?> unbindingClass;

	private String unbindingMethodName;

	private boolean hidden;

	private final List<ChildNode> children;
	private final List<ChildNode> effectiveChildren;

	private boolean finalisedProperties;
	private boolean blocked;
	private List<DataBindingType<T>> baseType;

	public DataBindingTypeConfiguratorImpl() {
		children = new ArrayList<>();
		effectiveChildren = new ArrayList<>();

		finalisedProperties = false;
	}

	@Override
	protected DataBindingType<T> tryCreate() {
		return new DataTypeImpl<>(this);
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
			public <U extends ChildNode> Set<U> overrideChild(String id,
					Class<U> nodeClass) {
				return Collections.emptySet();
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
	public DataBindingTypeConfigurator<T> isAbstract(boolean hidden) {
		requireConfigurable(this.hidden);
		this.hidden = hidden;

		return this;
	}

	@SafeVarargs
	@SuppressWarnings("unchecked")
	@Override
	public final <U extends T> DataBindingTypeConfigurator<U> baseType(
			DataBindingType<? super U>... baseType) {
		this.baseType = Arrays.asList((DataBindingType<T>[]) baseType);

		return (DataBindingTypeConfigurator<U>) this;
	}
}
