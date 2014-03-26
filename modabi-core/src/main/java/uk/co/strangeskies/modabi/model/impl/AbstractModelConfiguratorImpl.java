package uk.co.strangeskies.modabi.model.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.set.ListOrderedSet;

import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.building.AbstractModelConfigurator;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;
import uk.co.strangeskies.modabi.processing.SchemaResultProcessingContext;
import uk.co.strangeskies.modabi.processing.UnbindingStrategy;

public abstract class AbstractModelConfiguratorImpl<S extends AbstractModelConfigurator<S, N, T>, N extends AbstractModel<T>, T>
		extends BranchingNodeConfiguratorImpl<S, N> implements
		AbstractModelConfigurator<S, N, T> {
	protected static abstract class AbstractModelImpl<T> extends
			BranchingNodeImpl implements AbstractModel<T> {
		private final Class<T> dataClass;
		private final List<Model<? super T>> baseModel;
		private final Class<?> bindingClass;
		private final Class<?> unbindingClass;
		private final BindingStrategy bindingStrategy;
		private final UnbindingStrategy unbindingStrategy;
		private final Boolean isAbstract;

		public AbstractModelImpl(AbstractModelConfiguratorImpl<?, ?, T> configurator) {
			super(configurator);

			dataClass = configurator.dataClass;
			baseModel = configurator.baseModel == null ? new ArrayList<>()
					: new ArrayList<>(configurator.baseModel);
			bindingClass = configurator.bindingClass;
			unbindingClass = configurator.unbindingClass;
			bindingStrategy = configurator.bindingStrategy;
			unbindingStrategy = configurator.unbindingStrategy;
			isAbstract = configurator.isAbstract;
		}

		public AbstractModelImpl(AbstractModel<? super T> node,
				Collection<? extends AbstractModel<? super T>> overriddenNodes,
				List<ChildNode> effectiveChildren) {
			this(node, overriddenWithBase(node, overriddenNodes), effectiveChildren,
					null);
		}

		@SuppressWarnings("unchecked")
		private AbstractModelImpl(AbstractModel<? super T> node,
				Collection<AbstractModel<? super T>> overriddenNodes,
				List<ChildNode> effectiveChildren, Void flag) {
			super(node, overriddenNodes, effectiveChildren);

			dataClass = (Class<T>) getValue(node, overriddenNodes,
					n -> n.getDataClass(), (v, o) -> o.isAssignableFrom(v));

			baseModel = new ArrayList<>();
			overriddenNodes.forEach(n -> baseModel.addAll(n.getBaseModel()));
			baseModel.addAll(node.getBaseModel());

			bindingClass = getValue(node, overriddenNodes, n -> n.getBindingClass());

			unbindingClass = getValue(node, overriddenNodes,
					n -> n.getUnbindingClass());

			bindingStrategy = getValue(node, overriddenNodes,
					n -> n.getBindingStrategy());

			unbindingStrategy = getValue(node, overriddenNodes,
					n -> n.getUnbindingStrategy());

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
			/*
			 * for (int i = 0; i < baseModels.size(); i++) { Model<? super T>
			 * baseModel = baseModels.get(i);
			 * baseModels.addAll(baseModel.effectiveModel().getBaseModel()); }
			 */
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
		public final BindingStrategy getBindingStrategy() {
			return bindingStrategy;
		}

		@Override
		public final UnbindingStrategy getUnbindingStrategy() {
			return unbindingStrategy;
		}

		@Override
		public final Class<?> getBindingClass() {
			return bindingClass;
		}

		@Override
		public final Class<?> getUnbindingClass() {
			return unbindingClass;
		}

		@Override
		public void process(SchemaProcessingContext context) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <U> U process(SchemaResultProcessingContext<U> context) {
			throw new UnsupportedOperationException();
		}
	}

	private Class<T> dataClass;
	private List<Model<? super T>> baseModel;
	private Class<?> bindingClass;
	private Class<?> unbindingClass;
	private BindingStrategy bindingStrategy;
	private UnbindingStrategy unbindingStrategy;
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
			return bindingClass != null ? bindingClass : dataClass;
		else
			return getChildren().get(getChildren().size() - 1).getPostInputClass();
	}

	@Override
	public final S bindingClass(Class<?> bindingClass) {
		requireConfigurable(this.bindingClass);
		this.bindingClass = bindingClass;

		return getThis();
	}

	@Override
	public S unbindingClass(Class<?> unbindingClass) {
		requireConfigurable(this.unbindingClass);
		this.unbindingClass = unbindingClass;

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
}
