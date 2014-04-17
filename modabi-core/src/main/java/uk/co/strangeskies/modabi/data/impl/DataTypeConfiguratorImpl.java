package uk.co.strangeskies.modabi.data.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.co.strangeskies.gears.utilities.factory.Configurator;
import uk.co.strangeskies.gears.utilities.factory.InvalidBuildStateException;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.data.DataTypeConfigurator;
import uk.co.strangeskies.modabi.model.building.DataNodeConfigurator;
import uk.co.strangeskies.modabi.model.impl.BindingNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.impl.DataNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.impl.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
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

		private final List<DataNode<?>> properties;

		public DataTypeImpl(DataTypeConfiguratorImpl<T> configurator) {
			name = configurator.name;
			dataClass = configurator.dataClass;

			bindingStrategy = configurator.bindingStrategy;
			bindingClass = configurator.bindingClass;

			unbindingStrategy = configurator.unbindingStrategy;
			unbindingClass = configurator.unbindingClass;

			unbindingMethodName = configurator.unbindingMethodName;
			unbindingMethod = BindingNodeConfiguratorImpl.findUnbindingMethod(name,
					unbindingStrategy, unbindingMethodName, unbindingClass, dataClass);

			properties = Collections.unmodifiableList(new ArrayList<>(
					configurator.properties));
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
		public final List<DataNode<?>> getChildren() {
			return properties;
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

	private final List<DataNode<?>> properties;

	private boolean finalisedProperties;

	public DataTypeConfiguratorImpl() {
		properties = new ArrayList<>();

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

	public Class<?> getCurrentChildOutputTargetClass() {
		return unbindingClass != null ? unbindingClass : dataClass;
	}

	public Class<?> getCurrentChildInputTargetClass() {
		return dataClass; // TODO
	}

	@Override
	public DataNodeConfigurator<Object> addProperty() {
		SchemaNodeConfigurationContext<DataNode<Object>> context = new SchemaNodeConfigurationContext<DataNode<Object>>() {
			@Override
			public <U extends DataNode<Object>> List<U> overrideChild(String id,
					Class<U> nodeClass) {
				return Collections.emptyList();
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
			public void addChild(DataNode<Object> result, DataNode<Object> effective) {
				properties.add(result);
			}
		};

		return new DataNodeConfiguratorImpl<Object>(context);
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
