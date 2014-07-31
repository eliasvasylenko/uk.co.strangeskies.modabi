package uk.co.strangeskies.modabi.model.building.configurators.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
		BindingChildNodeConfiguratorImpl<ElementNodeConfigurator<T>, ElementNode<T>, T, ChildNode<?>, BindingChildNode<?, ?>>
		implements ElementNodeConfigurator<T> {
	protected static class ElementNodeImpl<T> extends
			BindingChildNodeImpl<T, ElementNode.Effective<T>> implements
			ElementNode<T> {
		private static class Effective<T> extends
				BindingChildNodeImpl.Effective<T, ElementNode.Effective<T>> implements
				ElementNode.Effective<T> {
			private final List<Model.Effective<? super T>> baseModel;
			private final Boolean isAbstract;

			protected Effective(
					OverrideMerge<ElementNode<T>, ElementNodeConfiguratorImpl<T>> overrideMerge) {
				super(overrideMerge);

				List<Model<? super T>> baseModel = new ArrayList<>();
				overrideMerge.configurator().getOverriddenNodes()
						.forEach(n -> baseModel.addAll(n.baseModel()));
				baseModel.addAll(overrideMerge.node().baseModel());

				this.baseModel = baseModel.stream().map(Model::effective)
						.collect(Collectors.toList());

				isAbstract = overrideMerge.getValue(ElementNode::isAbstract);
			}

			@Override
			public Boolean isAbstract() {
				return isAbstract;
			}

			@Override
			public List<Model.Effective<? super T>> baseModel() {
				return baseModel;
			}
		}

		private final Effective<T> effective;

		private final List<Model<? super T>> baseModel;
		private final Boolean isAbstract;

		public ElementNodeImpl(ElementNodeConfiguratorImpl<T> configurator) {
			super(configurator);

			baseModel = configurator.baseModel == null ? new ArrayList<>()
					: new ArrayList<>(configurator.baseModel);
			isAbstract = configurator.isAbstract;

			effective = new Effective<>(OverrideMerge.with(this, configurator));
		}

		@Override
		public Effective<T> effective() {
			return effective;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ElementNode))
				return false;

			ElementNode<?> other = (ElementNode<?>) obj;
			return super.equals(obj) && Objects.equals(baseModel, other.baseModel())
					&& Objects.equals(isAbstract, other.isAbstract());
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

		return (ElementNodeConfigurator<V>) this;
	}

	@Override
	protected Set<ElementNode<T>> getOverriddenNodes() {
		Set<ElementNode<T>> overriddenNodes = new HashSet<>(
				super.getOverriddenNodes());

		for (Model<? super T> base : baseModel)
			overriddenNodes.add(new ElementNodeWrapper<T>(base.effective(),
					getDataClass()));

		return overriddenNodes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ElementNodeConfigurator<V> dataClass(Class<V> dataClass) {
		return (ElementNodeConfigurator<V>) super.dataClass(dataClass);
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
	public ChildBuilder<ChildNode<?>, BindingChildNode<?, ?>> addChild() {
		return childBuilder();
	}
}
