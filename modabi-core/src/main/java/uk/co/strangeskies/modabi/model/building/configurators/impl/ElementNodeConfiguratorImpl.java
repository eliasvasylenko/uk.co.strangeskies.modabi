package uk.co.strangeskies.modabi.model.building.configurators.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.DataLoader;
import uk.co.strangeskies.modabi.model.building.configurators.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.ElementNodeWrapper;
import uk.co.strangeskies.modabi.model.building.impl.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.ElementNode;

public class ElementNodeConfiguratorImpl<T>
		extends
		BindingChildNodeConfiguratorImpl<ElementNodeConfigurator<T>, ElementNode<T>, T, ChildNode, BindingChildNode<?>>
		implements ElementNodeConfigurator<T> {
	protected static class ElementNodeImpl<T> extends BindingChildNodeImpl<T>
			implements ElementNode<T> {
		private final List<Model<? super T>> baseModel;
		private final Boolean isAbstract;

		public ElementNodeImpl(ElementNodeConfiguratorImpl<T> configurator) {
			super(configurator);

			baseModel = configurator.baseModel == null ? new ArrayList<>()
					: new ArrayList<>(configurator.baseModel);
			isAbstract = configurator.isAbstract;
		}

		public ElementNodeImpl(ElementNode<T> node,
				Collection<? extends ElementNode<? super T>> overriddenNodes,
				List<ChildNode> effectiveChildren, Class<?> outputTargetClass,
				Class<?> inputTargetClass) {
			this(node, overriddenWithBase(node, overriddenNodes), effectiveChildren,
					outputTargetClass, inputTargetClass, null);
		}

		private ElementNodeImpl(ElementNode<T> node,
				Collection<ElementNode<? super T>> overriddenNodes,
				List<ChildNode> effectiveChildren, Class<?> outputTargetClass,
				Class<?> inputTargetClass, Void flag) {
			super(node, overriddenNodes, effectiveChildren, outputTargetClass,
					inputTargetClass);

			baseModel = new ArrayList<>();
			overriddenNodes.forEach(n -> baseModel.addAll(n.baseModel()));
			baseModel.addAll(node.baseModel());

			OverrideMerge<ElementNode<? super T>> overrideMerge = new OverrideMerge<>(
					node, overriddenNodes);

			isAbstract = overrideMerge.getValue(n -> n.isAbstract());
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ElementNode))
				return false;

			ElementNode<?> other = (ElementNode<?>) obj;
			return super.equals(obj) && Objects.equals(baseModel, other.baseModel())
					&& Objects.equals(isAbstract, other.isAbstract());
		}

		protected static <T> Collection<ElementNode<? super T>> overriddenWithBase(
				ElementNode<? super T> node,
				Collection<? extends ElementNode<? super T>> overriddenNodes) {
			List<ElementNode<? super T>> overriddenAndModelNodes = new ArrayList<>();

			overriddenAndModelNodes.addAll(overriddenNodes);
			for (Model<? super T> base : node.baseModel())
				overriddenAndModelNodes.add(new ElementNodeWrapper<>(base));

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

	private List<Model<? super T>> baseModel;
	private Boolean isAbstract;

	public ElementNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super ElementNode<T>> parent) {
		super(parent);
	}

	@Override
	protected DataLoader getDataLoader() {
		return getContext().getDataLoader();
	}

	@Override
	public final ElementNodeConfigurator<T> isAbstract(boolean isAbstract) {
		requireConfigurable(this.isAbstract);
		this.isAbstract = isAbstract;

		return getThis();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ElementNodeConfigurator<V> baseModel(
			Model<? super V>... base) {
		requireConfigurable(this.baseModel);
		baseModel = Arrays.asList((Model<? super T>[]) base);

		baseModel.forEach(m -> {
			getChildren().inheritChildren(m.effectiveModel().getChildren());
		});

		return (ElementNodeConfigurator<V>) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ElementNodeConfigurator<V> dataClass(Class<V> dataClass) {
		return (ElementNodeConfigurator<V>) super.dataClass(dataClass);
	}

	@Override
	protected ElementNode<T> getEffective(ElementNode<T> node) {
		return new ElementNodeImpl<T>(node, getOverriddenNodes(), getChildren()
				.getEffectiveChildren(), getContext()
				.getCurrentChildOutputTargetClass(), getCurrentChildInputTargetClass());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Class<ElementNode<T>> getNodeClass() {
		return (Class<ElementNode<T>>) (Object) ElementNode.class;
	}

	@Override
	protected ElementNode<T> tryCreate() {
		return new ElementNodeImpl<>(this);
	}

	@Override
	public ChildBuilder<ChildNode, BindingChildNode<?>> addChild() {
		return childBuilder();
	}
}
