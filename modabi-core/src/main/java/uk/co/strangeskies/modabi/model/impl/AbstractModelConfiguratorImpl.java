package uk.co.strangeskies.modabi.model.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.set.ListOrderedSet;

import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.ImplementationStrategy;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.building.AbstractModelConfigurator;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;

public abstract class AbstractModelConfiguratorImpl<S extends AbstractModelConfigurator<S, N, T>, N extends AbstractModel<T>, T>
		extends BranchingNodeConfiguratorImpl<S, N> implements
		AbstractModelConfigurator<S, N, T> {
	protected static abstract class AbstractModelImpl<T> extends
			BranchingNodeImpl implements AbstractModel<T> {
		private final Class<T> dataClass;
		private final List<Model<? super T>> baseModel;
		private final String buildMethodName;
		private final Class<?> builderClass;
		private final ImplementationStrategy implementationStrategy;
		private final Boolean isAbstract;

		public AbstractModelImpl(AbstractModelConfiguratorImpl<?, ?, T> configurator) {
			super(configurator);

			dataClass = configurator.dataClass;
			baseModel = configurator.baseModel == null ? new ArrayList<>()
					: new ArrayList<>(configurator.baseModel);
			buildMethodName = configurator.buildMethodName;
			builderClass = configurator.builderClass;
			implementationStrategy = configurator.bindingStrategy;
			isAbstract = configurator.isAbstract;
		}

		public AbstractModelImpl(AbstractModel<? super T> node,
				Collection<? extends AbstractModel<? super T>> overriddenNodes,
				List<SchemaNode> effectiveChildren) {
			this(node, overriddenWithBase(node, overriddenNodes), effectiveChildren,
					null);
		}

		@SuppressWarnings("unchecked")
		private AbstractModelImpl(AbstractModel<? super T> node,
				Collection<AbstractModel<? super T>> overriddenNodes,
				List<SchemaNode> effectiveChildren, Void flag) {
			super(node, overriddenNodes, effectiveChildren);

			dataClass = (Class<T>) getValue(node, overriddenNodes,
					n -> n.getDataClass(), (v, o) -> o.isAssignableFrom(v));

			baseModel = new ArrayList<>();
			overriddenNodes.forEach(n -> baseModel.addAll(n.getBaseModel()));
			baseModel.addAll(node.getBaseModel());

			buildMethodName = getValue(node, overriddenNodes,
					n -> n.getBuilderMethod());

			builderClass = getValue(node, overriddenNodes, n -> n.getBuilderClass());

			implementationStrategy = getValue(node, overriddenNodes,
					n -> n.getImplementationStrategy());

			isAbstract = getValue(node, overriddenNodes, n -> n.isAbstract());
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof AbstractModel))
				return false;
			return super.equals(obj);
		}

		private static <T> Collection<AbstractModel<? super T>> overriddenWithBase(
				AbstractModel<? super T> node,
				Collection<? extends AbstractModel<? super T>> overriddenNodes) {
			List<AbstractModel<? super T>> overriddenAndModelNodes = new ArrayList<>();
			overriddenAndModelNodes.addAll(overriddenNodes);

			ListOrderedSet<Model<? super T>> baseModels = new ListOrderedSet<>();
			baseModels.addAll(node.getBaseModel());
			/*for (int i = 0; i < baseModels.size(); i++) {
				Model<? super T> baseModel = baseModels.get(i);
				baseModels.addAll(baseModel.effectiveModel().getBaseModel());
			}*/
			overriddenAndModelNodes.addAll(baseModels);

			return overriddenAndModelNodes;
		}

		@Override
		public final Boolean isAbstract() {
			return isAbstract;
		}

		@Override
		public final List<Model<? super T>> getBaseModel() {
			return baseModel;
		}

		@Override
		public final Class<T> getDataClass() {
			return dataClass;
		}

		@Override
		public final ImplementationStrategy getImplementationStrategy() {
			return implementationStrategy;
		}

		@Override
		public final Class<?> getBuilderClass() {
			return builderClass;
		}

		@Override
		public final String getBuilderMethod() {
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
		requireConfigurable(this.isAbstract);
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
		requireConfigurable(this.baseModel);
		AbstractModelConfiguratorImpl<?, ?, V> thisV = (AbstractModelConfiguratorImpl<?, ?, V>) this;
		thisV.baseModel = Arrays.asList(base);

		baseModel.forEach(m -> {
			inheritChildren(m.effectiveModel().getChildren().stream()
					.collect(Collectors.toList()));
		});

		return thisV;
	}

	protected final List<Model<? super T>> getBaseModel() {
		return baseModel;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> AbstractModelConfigurator<?, ?, V> dataClass(
			Class<V> dataClass) {
		requireConfigurable(this.dataClass);
		this.dataClass = (Class<T>) dataClass;

		return (AbstractModelConfigurator<?, ?, V>) this;
	}

	@Override
	protected final Class<T> getDataClass() {
		return dataClass;
	}

	@Override
	protected Class<?> getCurrentChildPreInputClass() {
		if (getChildren().isEmpty())
			return builderClass != null ? builderClass : dataClass;
		else
			return getChildren().get(getChildren().size() - 1).getPostInputClass();
	}

	@Override
	public final S builderClass(Class<?> factoryClass) {
		requireConfigurable(this.builderClass);
		this.builderClass = factoryClass;

		return getThis();
	}

	protected final Class<?> getBuilderClass() {
		return builderClass;
	}

	@Override
	public final S builderMethod(String buildMethodName) {
		requireConfigurable(this.buildMethodName);
		this.buildMethodName = buildMethodName;

		return getThis();
	}

	protected final String getBuilderMethod() {
		return buildMethodName;
	}

	@Override
	public final S implementationStrategy(ImplementationStrategy bindingStrategy) {
		requireConfigurable(this.bindingStrategy);
		this.bindingStrategy = bindingStrategy;

		return getThis();
	}

	protected final ImplementationStrategy getImplementationStrategy() {
		return bindingStrategy;
	}
}
