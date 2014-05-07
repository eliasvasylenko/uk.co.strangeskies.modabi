package uk.co.strangeskies.modabi.data.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import uk.co.strangeskies.gears.utilities.factory.Configurator;
import uk.co.strangeskies.gears.utilities.factory.InvalidBuildStateException;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.data.DataTypeConfigurator;
import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.DataNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.impl.BindingNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.impl.ChoiceNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.impl.DataNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.impl.InputSequenceNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.impl.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.model.impl.SequenceNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.DataNodeChildNode;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.UnbindingStrategy;

public class DataTypeConfiguratorImpl<T> extends Configurator<DataType<T>>
		implements DataTypeConfigurator<T> {
	public static class DataTypeImpl<T> implements DataType<T> {
		private final String name;
		private final Class<T> dataClass;

		private final BindingStrategy bindingStrategy;
		private final Class<?> bindingClass;

		private final UnbindingStrategy unbindingStrategy;
		private final Class<?> unbindingClass;
		private final String unbindingMethodName;
		private final Method unbindingMethod;

		private final List<ChildNode> children;
		private final List<ChildNode> effectiveChildren;

		public DataTypeImpl(DataTypeConfiguratorImpl<T> configurator) {
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
		public final List<ChildNode> getChildren() {
			return children;
		}

		@Override
		public List<ChildNode> getEffectiveChildren() {
			return effectiveChildren;
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

	private final List<ChildNode> children;
	private final List<ChildNode> effectiveChildren;

	private boolean finalisedProperties;
	private boolean blocked;

	public DataTypeConfiguratorImpl() {
		children = new ArrayList<>();
		effectiveChildren = new ArrayList<>();

		finalisedProperties = false;
	}

	@Override
	protected DataType<T> tryCreate() {
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
	public DataTypeConfigurator<T> name(String name) {
		requireConfigurable(this.name);
		this.name = name;

		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U extends T> DataTypeConfigurator<U> dataClass(Class<U> dataClass) {
		requireConfigurable(this.dataClass);
		this.dataClass = (Class<T>) dataClass;

		return (DataTypeConfigurator<U>) this;
	}

	@Override
	public DataTypeConfigurator<T> bindingClass(Class<?> bindingClass) {
		requireConfigurable(this.bindingClass);
		this.bindingClass = bindingClass;

		return this;
	}

	@Override
	public DataTypeConfigurator<T> unbindingMethod(String name) {
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
				return DataTypeConfiguratorImpl.this.getCurrentChildOutputTargetClass();
			}

			@Override
			public Class<?> getCurrentChildInputTargetClass() {
				return DataTypeConfiguratorImpl.this.getCurrentChildInputTargetClass();
			}

			@Override
			public void addChild(ChildNode result, ChildNode effective) {
				DataTypeConfiguratorImpl.this.addChild(result, effective);
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
	public DataTypeConfigurator<T> bindingStrategy(BindingStrategy strategy) {
		requireConfigurable(bindingStrategy);
		bindingStrategy = strategy;

		return this;
	}

	@Override
	public DataTypeConfigurator<T> unbindingStrategy(UnbindingStrategy strategy) {
		requireConfigurable(unbindingStrategy);
		unbindingStrategy = strategy;

		return this;
	}

	@Override
	public DataTypeConfigurator<T> unbindingClass(Class<?> unbindingClass) {
		requireConfigurable(this.unbindingClass);
		this.unbindingClass = unbindingClass;

		return this;
	}
}
