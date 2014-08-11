package uk.co.strangeskies.modabi.model.building.configurators.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import uk.co.strangeskies.gears.utilities.IdentityComparator;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.building.DataLoader;
import uk.co.strangeskies.modabi.model.building.configurators.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.ElementNodeWrapper;
import uk.co.strangeskies.modabi.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.model.building.impl.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;

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
			private final Set<Model.Effective<? super T>> baseModel;
			private final Boolean isAbstract;

			protected Effective(
					OverrideMerge<ElementNode<T>, ElementNodeConfiguratorImpl<T>> overrideMerge) {
				super(overrideMerge);

				Set<Model.Effective<? super T>> baseModel = new TreeSet<>(
						new IdentityComparator<>());
				overrideMerge.configurator().getOverriddenNodes()
						.forEach(n -> baseModel.addAll(n.effective().baseModel()));
				baseModel.addAll(overrideMerge.node().baseModel().stream()
						.map(SchemaNode::effective).collect(Collectors.toSet()));
				this.baseModel = Collections.unmodifiableSet(baseModel);

				isAbstract = overrideMerge.getValue(ElementNode::isAbstract);
			}

			@Override
			public Boolean isAbstract() {
				return isAbstract;
			}

			@Override
			public Set<Model.Effective<? super T>> baseModel() {
				return baseModel;
			}
		}

		private final Effective<T> effective;

		private final Set<Model<? super T>> baseModel;
		private final Boolean isAbstract;

		public ElementNodeImpl(ElementNodeConfiguratorImpl<T> configurator) {
			super(configurator);

			baseModel = configurator.baseModel == null ? Collections.emptySet()
					: new HashSet<>(configurator.baseModel);
			isAbstract = configurator.isAbstract;

			effective = new Effective<>(overrideMerge(this, configurator));
		}

		@Override
		public Effective<T> effective() {
			return effective;
		}

		@Override
		public final Boolean isAbstract() {
			return isAbstract;
		}

		@Override
		public final Set<Model<? super T>> baseModel() {
			return baseModel;
		}
	}

	private Set<Model<? super T>> baseModel;
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
		baseModel = new HashSet<>(Arrays.asList((Model<T>[]) base));

		return (ElementNodeConfigurator<V>) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected LinkedHashSet<ElementNode<T>> getOverriddenNodes() {
		LinkedHashSet<ElementNode<T>> overriddenNodes = new LinkedHashSet<>();

		if (baseModel != null)
			for (Model<? super T> base : baseModel)
				overriddenNodes.add(new ElementNodeWrapper<T>(base.effective(),
						(Class<T>) base.effective().getDataClass())); // TODO sanity check
																													// when not tired as
																													// balls, probs can be
																													// made more sensible

		overriddenNodes.addAll(super.getOverriddenNodes());

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
	protected boolean isAbstract() {
		return (isAbstract != null && isAbstract)
				|| getOverriddenNodes().stream().anyMatch(
						m -> m.effective().isAbstract() != null
								&& m.effective().isAbstract());
	}
}
