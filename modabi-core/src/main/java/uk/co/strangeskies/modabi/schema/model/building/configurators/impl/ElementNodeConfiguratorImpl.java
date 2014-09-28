package uk.co.strangeskies.modabi.schema.model.building.configurators.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.model.Model;
import uk.co.strangeskies.modabi.schema.model.building.configurators.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.impl.ElementNodeWrapper;
import uk.co.strangeskies.modabi.schema.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.schema.model.building.impl.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;

public class ElementNodeConfiguratorImpl<T>
		extends
		BindingChildNodeConfiguratorImpl<ElementNodeConfigurator<T>, ElementNode<T>, T, ChildNode<?, ?>, BindingChildNode<?, ?, ?>>
		implements ElementNodeConfigurator<T> {
	protected static class ElementNodeImpl<T> extends
			BindingChildNodeImpl<T, ElementNode<T>, ElementNode.Effective<T>>
			implements ElementNode<T> {
		private static class Effective<T>
				extends
				BindingChildNodeImpl.Effective<T, ElementNode<T>, ElementNode.Effective<T>>
				implements ElementNode.Effective<T> {
			private final List<Model.Effective<? super T>> baseModel;

			protected Effective(
					OverrideMerge<ElementNode<T>, ElementNodeConfiguratorImpl<T>> overrideMerge) {
				super(overrideMerge);

				List<Model.Effective<? super T>> baseModel = new ArrayList<>();
				overrideMerge.configurator().getOverriddenNodes()
						.forEach(n -> baseModel.addAll(n.effective().baseModel()));
				baseModel.addAll(overrideMerge.node().baseModel().stream()
						.map(SchemaNode::effective).collect(Collectors.toSet()));
				this.baseModel = Collections.unmodifiableList(baseModel);
			}

			@Override
			public List<Model.Effective<? super T>> baseModel() {
				return baseModel;
			}
		}

		private final Effective<T> effective;

		private final List<Model<? super T>> baseModel;

		public ElementNodeImpl(ElementNodeConfiguratorImpl<T> configurator) {
			super(configurator);

			baseModel = configurator.baseModel == null ? Collections.emptyList()
					: Collections
							.unmodifiableList(new ArrayList<>(configurator.baseModel));

			effective = new Effective<>(overrideMerge(this, configurator));
		}

		@Override
		public Effective<T> effective() {
			return effective;
		}

		@Override
		public final List<Model<? super T>> baseModel() {
			return baseModel;
		}
	}

	private List<Model<? super T>> baseModel;

	public ElementNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super ElementNode<T>> parent) {
		super(parent);
	}

	@Override
	public ElementNodeConfigurator<T> name(String name) {
		return name(new QualifiedName(name, getContext().getNamespace()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ElementNodeConfigurator<V> baseModel(
			List<? extends Model<? super V>> base) {
		requireConfigurable(this.baseModel);
		baseModel = new ArrayList<>((List<? extends Model<? super T>>) base);

		return (ElementNodeConfigurator<V>) this;
	}

	@Override
	public List<ElementNode<T>> getOverriddenNodes() {
		List<ElementNode<T>> overriddenNodes = new ArrayList<>();

		if (baseModel != null)
			for (Model<? super T> base : baseModel)
				overriddenNodes.add(new ElementNodeWrapper<>(base.effective()));

		overriddenNodes.addAll(super.getOverriddenNodes());

		return overriddenNodes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ElementNodeConfigurator<V> dataClass(Class<V> dataClass) {
		return (ElementNodeConfigurator<V>) super.dataClass(dataClass);
	}

	@Override
	protected boolean isDataContext() {
		return false;
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
}
