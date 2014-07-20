package uk.co.strangeskies.modabi.model.building.impl.configurators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.EffectiveModel;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.DataLoader;
import uk.co.strangeskies.modabi.model.building.configurators.ModelConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;

public class ModelConfiguratorImpl<T>
		extends
		BindingNodeConfiguratorImpl<ModelConfigurator<T>, Model<T>, T, ChildNode, BindingChildNode<?>>
		implements ModelConfigurator<T> {
	protected static abstract class AbstractModelImpl<T> extends
			BindingNodeImpl<T> implements AbstractModel<T> {
		private final List<Model<? super T>> baseModel;
		private final Boolean isAbstract;

		public AbstractModelImpl(ModelConfiguratorImpl<T> configurator) {
			super(configurator);

			baseModel = configurator.baseModel == null ? new ArrayList<>()
					: new ArrayList<>(configurator.baseModel);
			isAbstract = configurator.isAbstract;
		}

		public AbstractModelImpl(AbstractModel<T> node,
				Collection<? extends AbstractModel<? super T>> overriddenNodes,
				List<ChildNode> effectiveChildren) {
			this(node, overriddenWithBase(node, overriddenNodes), effectiveChildren,
					null);
		}

		private AbstractModelImpl(AbstractModel<T> node,
				Collection<AbstractModel<? super T>> overriddenNodes,
				List<ChildNode> effectiveChildren, Void flag) {
			super(node, overriddenNodes, effectiveChildren);

			baseModel = new ArrayList<>();
			overriddenNodes.forEach(n -> baseModel.addAll(n.baseModel()));
			baseModel.addAll(node.baseModel());

			OverrideMerge<AbstractModel<? super T>> overrideMerge = new OverrideMerge<>(
					node, overriddenNodes);

			isAbstract = overrideMerge.getValue(n -> n.isAbstract());
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof AbstractModel))
				return false;

			AbstractModel<?> other = (AbstractModel<?>) obj;
			return super.equals(obj) && Objects.equals(baseModel, other.baseModel())
					&& Objects.equals(isAbstract, other.isAbstract());
		}

		private static <T> Collection<AbstractModel<? super T>> overriddenWithBase(
				AbstractModel<? super T> node,
				Collection<? extends AbstractModel<? super T>> overriddenNodes) {
			List<AbstractModel<? super T>> overriddenAndModelNodes = new ArrayList<>();

			overriddenAndModelNodes.addAll(overriddenNodes);
			overriddenAndModelNodes.addAll(node.baseModel());

			return overriddenAndModelNodes;
		}

		@Override
		public final Boolean isAbstract() {
			return isAbstract;
		}

		@Override
		public final List<Model<? super T>> baseModel() {
			return baseModel;
		}
	}

	protected static class EffectiveModelImpl<T> extends AbstractModelImpl<T>
			implements EffectiveModel<T> {
		public EffectiveModelImpl(ModelImpl<T> node,
				Collection<? extends EffectiveModel<? super T>> overriddenNodes,
				List<ChildNode> effectiveChildren) {
			super(node, overriddenNodes, effectiveChildren);
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof EffectiveModel))
				return false;
			return super.equals(obj);
		}
	}

	protected static class ModelImpl<T> extends AbstractModelImpl<T> implements
			Model<T> {
		private final EffectiveModel<T> effectiveModel;

		public ModelImpl(ModelConfiguratorImpl<T> configurator) {
			super(configurator);

			effectiveModel = new EffectiveModelImpl<T>(this, baseModel().stream()
					.map(m -> m.effectiveModel()).collect(Collectors.toList()),
					configurator.getEffectiveChildren());
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ModelImpl))
				return false;
			return super.equals(obj);
		}

		@Override
		public EffectiveModel<T> effectiveModel() {
			return effectiveModel;
		}
	}

	private final DataLoader loader;

	private List<Model<? super T>> baseModel;
	private Boolean isAbstract;

	public ModelConfiguratorImpl(DataLoader loader) {
		this.loader = loader;
	}

	@Override
	protected DataLoader getDataLoader() {
		return loader;
	}

	@Override
	public final ModelConfigurator<T> isAbstract(boolean isAbstract) {
		requireConfigurable(this.isAbstract);
		this.isAbstract = isAbstract;

		return getThis();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ModelConfigurator<V> baseModel(Model<? super V>... base) {
		requireConfigurable(this.baseModel);
		baseModel = Arrays.asList((Model<T>[]) base);

		baseModel.forEach(m -> {
			inheritChildren(m.effectiveModel().getChildren());
		});

		return (ModelConfigurator<V>) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ModelConfigurator<V> dataClass(Class<V> dataClass) {
		return (ModelConfigurator<V>) super.dataClass(dataClass);
	}

	@Override
	public Model<T> tryCreate() {
		return new ModelImpl<>(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Class<Model<T>> getNodeClass() {
		return (Class<Model<T>>) (Object) Model.class;
	}

	@Override
	protected Model<T> getEffective(Model<T> node) {
		return null;
	}

	@Override
	public ChildBuilder<ChildNode, BindingChildNode<?>> addChild() {
		return childBuilder();
	}
}
