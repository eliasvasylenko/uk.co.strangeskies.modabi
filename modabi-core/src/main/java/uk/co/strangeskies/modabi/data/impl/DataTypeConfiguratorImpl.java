package uk.co.strangeskies.modabi.data.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import uk.co.strangeskies.gears.utilities.factory.Configurator;
import uk.co.strangeskies.gears.utilities.factory.InvalidBuildStateException;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.data.DataSink;
import uk.co.strangeskies.modabi.data.DataSource;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.data.DataTypeConfigurator;
import uk.co.strangeskies.modabi.data.DataTypeRestrictionConfigurator;
import uk.co.strangeskies.modabi.data.DataTypeRestrictions;
import uk.co.strangeskies.modabi.model.building.PropertyNodeConfigurator;
import uk.co.strangeskies.modabi.model.impl.PropertyNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.nodes.PropertyNode;
import uk.co.strangeskies.modabi.processing.impl.SchemaBinderImpl;

public class DataTypeConfiguratorImpl<T> extends Configurator<DataType<T>>
		implements DataTypeConfigurator<T> {
	public static class DataTypeImpl<T> implements DataType<T> {
		private final String name;
		private final DataType<?> baseType;
		private final DataTypeRestrictions<?> baseRestrictions;
		private final Class<T> dataClass;
		private final Class<?> builderClass;

		private final InputMethodStrategy inputStrategy;
		private final String inputMethodName;
		private Method inputMethod;
		private final OutputMethodStrategy outputStrategy;
		private final String outputMethodName;
		private Method outputMethod;

		private final String buildMethodName;
		private Method buildMethod;

		private final List<PropertyNode<?>> properties;

		public DataTypeImpl(DataTypeConfiguratorImpl<T> configurator) {
			name = configurator.name;
			baseType = configurator.baseType;
			baseRestrictions = configurator.baseRestrictions;
			dataClass = configurator.dataClass;
			builderClass = configurator.builderClass;

			inputStrategy = configurator.inputStrategy;
			inputMethodName = configurator.inputMethod;
			outputStrategy = configurator.outputStrategy;
			outputMethodName = configurator.outputMethod;
			buildMethodName = configurator.buildMethod;
			try {
				Class<?> baseTypeInputClass = baseType == null ? DataSource.class
						: baseType.getDataClass();
				List<String> inputMethodNames = inputMethodName == null ? null : Arrays
						.asList(inputMethodName);
				if (inputStrategy == InputMethodStrategy.PARSE) {
					if (inputMethodNames == null)
						inputMethodNames = SchemaBinderImpl.generateInMethodNames(baseType
								.getName());

					inputMethod = SchemaBinderImpl.findMethod(inputMethodNames,
							dataClass, null, baseTypeInputClass);
				} else if (inputStrategy == null
						|| inputStrategy == InputMethodStrategy.GET) {
					if (inputMethodNames == null)
						inputMethodNames = SchemaBinderImpl.generateOutMethodNames(name,
								false, null);

					inputMethod = SchemaBinderImpl.findMethod(inputMethodNames,
							baseTypeInputClass, dataClass);
				} else {
					throw new SchemaException(new AssertionError());
				}

				Class<?> baseTypeOutputClass = baseType == null ? DataSink.class
						: baseType.getDataClass();
				List<String> outputMethodNames = outputMethodName == null ? null
						: Arrays.asList(outputMethodName);
				if (outputStrategy == OutputMethodStrategy.COMPOSE) {
					if (outputMethodNames == null)
						outputMethodNames = SchemaBinderImpl.generateOutMethodNames(
								baseType.getName(), false, null);

					outputMethod = SchemaBinderImpl.findMethod(outputMethodNames,
							dataClass, baseTypeOutputClass);
				} else if (outputStrategy == null
						|| outputStrategy == OutputMethodStrategy.SET) {
					if (outputMethodNames == null)
						outputMethodNames = SchemaBinderImpl.generateInMethodNames(name);

					outputMethod = SchemaBinderImpl.findMethod(outputMethodNames,
							baseTypeOutputClass, null, dataClass);
				} else {
					throw new SchemaException(new AssertionError());
				}

				buildMethod = null;// configurator.buildMethod;
			} catch (NoSuchMethodException | SecurityException e) {
				// throw new SchemaException(e);
			}

			properties = Collections.unmodifiableList(new ArrayList<>(
					configurator.properties));
		}

		@Override
		public final String getName() {
			return name;
		}

		@Override
		public DataType<?> getBaseType() {
			return baseType;
		}

		@Override
		public DataTypeRestrictions<?> getBaseRestrictions() {
			return baseRestrictions;
		}

		@Override
		public final Class<T> getDataClass() {
			return dataClass;
		}

		@Override
		public final Class<?> getBuilderClass() {
			return builderClass;
		}

		@Override
		public InputMethodStrategy getInputMethodStrategy() {
			return inputStrategy;
		}

		@Override
		public final String getSourceMethodName() {
			return inputMethodName;
		}

		@Override
		public Method getSourceMethod() {
			return inputMethod;
		}

		@Override
		public OutputMethodStrategy getOutputMethodStrategy() {
			return outputStrategy;
		}

		@Override
		public String getSinkMethodName() {
			return outputMethodName;
		}

		@Override
		public Method getSinkMethod() {
			return outputMethod;
		}

		@Override
		public final String getBuildMethodName() {
			return buildMethodName;
		}

		@Override
		public Method getBuildMethod() {
			return buildMethod;
		}

		@Override
		public final List<PropertyNode<?>> getProperties() {
			return properties;
		}
	}

	private String name;
	private DataType<?> baseType;
	private DataTypeRestrictions<?> baseRestrictions;
	private Class<T> dataClass;
	private Class<?> builderClass;

	private InputMethodStrategy inputStrategy;
	private String inputMethod;
	private OutputMethodStrategy outputStrategy;
	private String outputMethod;
	private String buildMethod;

	private final List<PropertyNode<?>> properties;

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

	@Override
	public DataTypeConfigurator<T> baseType(DataType<?> baseType) {
		requireConfigurable(this.baseType);
		this.baseType = baseType;

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
	public DataTypeConfigurator<T> builderClass(Class<?> builderClass) {
		requireConfigurable(this.builderClass);
		this.builderClass = builderClass;

		return this;
	}

	@Override
	public DataTypeConfigurator<T> inputMethodStrategy(
			InputMethodStrategy strategy) {
		requireConfigurable(this.inputStrategy);
		this.inputStrategy = strategy;

		return this;
	}

	@Override
	public DataTypeConfigurator<T> inputMethod(String name) {
		requireConfigurable(this.inputMethod);
		this.inputMethod = name;

		return this;
	}

	@Override
	public DataTypeConfigurator<T> outputMethodStrategy(
			OutputMethodStrategy strategy) {
		requireConfigurable(this.outputStrategy);
		this.outputStrategy = strategy;

		return this;
	}

	@Override
	public DataTypeConfigurator<T> outputMethod(String name) {
		requireConfigurable(this.outputMethod);
		this.outputMethod = name;

		return this;
	}

	@Override
	public DataTypeConfigurator<T> buildMethod(String name) {
		requireConfigurable(this.buildMethod);
		this.buildMethod = name;

		return this;
	}

	@Override
	public PropertyNodeConfigurator<Object> addProperty() {
		return new PropertyNodeConfiguratorImpl<Object>(
				(id, nodeClass) -> Collections.emptyList(), (result, effective) -> {
				}, builderClass, dataClass);
	}

	@Override
	public DataTypeRestrictionConfigurator<Object> baseRestrictions() {
		// TODO Auto-generated method stub
		return null;
	}
}
