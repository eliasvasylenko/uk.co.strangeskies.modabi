package uk.co.strangeskies.modabi.model.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.ImplementationStrategy;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.building.AbstractModelConfigurator;

abstract class AbstractModelConfiguratorImpl<S extends AbstractModelConfigurator<S, N, T>, N extends AbstractModel<T>, T>
		extends BranchingNodeConfiguratorImpl<S, N> implements
		AbstractModelConfigurator<S, N, T> {
	protected static abstract class AbstractModelImpl<T> extends
			BranchingNodeImpl implements AbstractModel<T> {
		private final Class<T> dataClass;
		private final List<Model<? super T>> baseModel;
		private final String buildMethodName;
		private final Class<?> builderClass;
		private final ImplementationStrategy bindingStrategy;
		private final Boolean isAbstract;

		public AbstractModelImpl(AbstractModelConfiguratorImpl<?, ?, T> configurator) {
			super(configurator);

			dataClass = configurator.dataClass;
			baseModel = configurator.baseModel == null ? null : new ArrayList<>(
					configurator.baseModel);
			buildMethodName = configurator.buildMethodName;
			builderClass = configurator.builderClass;
			bindingStrategy = configurator.bindingStrategy;
			isAbstract = configurator.isAbstract;
		}

		@Override
		public Boolean isAbstract() {
			return isAbstract;
		}

		@Override
		public List<Model<? super T>> getBaseModel() {
			return baseModel;
		}

		@Override
		public Class<T> getDataClass() {
			return dataClass;
		}

		@Override
		public ImplementationStrategy getImplementationStrategy() {
			return bindingStrategy;
		}

		@Override
		public Class<?> getBuilderClass() {
			return builderClass;
		}

		@Override
		public String getBuilderMethod() {
			return buildMethodName;
		}
	}

	private Class<T> dataClass;
	private List<Model<? super T>> baseModel;
	private String buildMethodName;
	private Class<?> builderClass;
	private ImplementationStrategy bindingStrategy;
	private Boolean isAbstract;

	public AbstractModelConfiguratorImpl(
			BranchingNodeConfiguratorImpl<?, ?> parent) {
		super(parent);
	}

	@Override
	public final S isAbstract(boolean isAbstract) {
		assertConfigurable(this.isAbstract);
		this.isAbstract = isAbstract;

		return getThis();
	}

	protected final Boolean isAbstract() {
		return isAbstract;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> AbstractModelConfigurator<?, ?, V> baseModel(
			Model<? super V>... base) {
		assertConfigurable(this.baseModel);
		AbstractModelConfiguratorImpl<?, ?, V> thisV = (AbstractModelConfiguratorImpl<?, ?, V>) this;
		thisV.baseModel = Arrays.asList(base);

		// TODO add nodes to be overridden & check for conflicts

		return thisV;
	}

	protected final List<Model<? super T>> getBaseModel() {
		return baseModel;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> AbstractModelConfigurator<?, ?, V> dataClass(
			Class<V> dataClass) {
		assertConfigurable(this.dataClass);
		this.dataClass = (Class<T>) dataClass;

		return (AbstractModelConfigurator<?, ?, V>) this;
	}

	protected final Class<T> getDataClass() {
		return dataClass;
	}

	@Override
	public final S builderClass(Class<?> factoryClass) {
		assertConfigurable(this.builderClass);
		this.builderClass = factoryClass;

		return getThis();
	}

	protected final Class<?> getBuilderClass() {
		return builderClass;
	}

	@Override
	public final S builderMethod(String buildMethodName) {
		assertConfigurable(this.buildMethodName);
		this.buildMethodName = buildMethodName;

		return getThis();
	}

	protected final String getBuilderMethod() {
		return buildMethodName;
	}

	@Override
	public final S implementationStrategy(ImplementationStrategy bindingStrategy) {
		assertConfigurable(this.bindingStrategy);
		this.bindingStrategy = bindingStrategy;

		return getThis();
	}

	protected final ImplementationStrategy getImplementationStrategy() {
		return bindingStrategy;
	}
}
