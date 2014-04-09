package uk.co.strangeskies.modabi.model.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.processing.UnbindingContext;

public class ElementNodeConfiguratorImpl<T>
		extends
		BindingChildNodeConfiguratorImpl<ElementNodeConfigurator<T>, ElementNode<T>, T>
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
				List<ChildNode> effectiveChildren, Class<?> parentClass) {
			this(node, overriddenWithBase(node, overriddenNodes), effectiveChildren,
					parentClass, null);
		}

		private ElementNodeImpl(ElementNode<T> node,
				Collection<ElementNode<? super T>> overriddenNodes,
				List<ChildNode> effectiveChildren, Class<?> parentClass, Void flag) {
			super(node, overriddenNodes, effectiveChildren, parentClass);

			baseModel = new ArrayList<>();
			overriddenNodes.forEach(n -> baseModel.addAll(n.getBaseModel()));
			baseModel.addAll(node.getBaseModel());

			isAbstract = getValue(node, overriddenNodes, n -> n.isAbstract());
		}

		protected static <T> Collection<ElementNode<? super T>> overriddenWithBase(
				ElementNode<? super T> node,
				Collection<? extends ElementNode<? super T>> overriddenNodes) {
			List<ElementNode<? super T>> overriddenAndModelNodes = new ArrayList<>();

			overriddenAndModelNodes.addAll(overriddenNodes);
			for (Model<? super T> base : node.getBaseModel())
				overriddenAndModelNodes.add(new ElementNodeWrapper<>(base));

			return overriddenAndModelNodes;
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
		protected void unbind(UnbindingContext context) {
			go!
		}
	}

	private List<Model<? super T>> baseModel;
	private Boolean isAbstract;

	public ElementNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super ElementNode<T>> parent) {
		super(parent);
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
		ElementNodeConfiguratorImpl<V> thisV = (ElementNodeConfiguratorImpl<V>) this;
		thisV.baseModel = Arrays.asList(base);

		baseModel.forEach(m -> {
			inheritChildren(m.effectiveModel().getChildren());
		});

		return thisV;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ElementNodeConfigurator<V> dataClass(Class<V> dataClass) {
		return (ElementNodeConfigurator<V>) super.dataClass(dataClass);
	}

	@Override
	protected ElementNode<T> getEffective(ElementNode<T> node) {
		return new ElementNodeImpl<T>(node, getOverriddenNodes(),
				getEffectiveChildren(), getContext().getCurrentChildOutputTargetClass());
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
	public ChildBuilder addChild() {
		return super.addChild();
	}
}
