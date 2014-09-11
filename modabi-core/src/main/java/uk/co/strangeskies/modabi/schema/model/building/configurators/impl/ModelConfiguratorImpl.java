package uk.co.strangeskies.modabi.schema.model.building.configurators.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.schema.model.Model;
import uk.co.strangeskies.modabi.schema.model.building.DataLoader;
import uk.co.strangeskies.modabi.schema.model.building.configurators.ModelConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;
import uk.co.strangeskies.utilities.IdentityComparator;

public class ModelConfiguratorImpl<T>
		extends
		BindingNodeConfiguratorImpl<ModelConfigurator<T>, Model<T>, T, ChildNode<?, ?>, BindingChildNode<?, ?, ?>>
		implements ModelConfigurator<T> {
	protected static class ModelImpl<T> extends
			BindingNodeImpl<T, Model<T>, Model.Effective<T>> implements Model<T> {
		private static class Effective<T> extends
				BindingNodeImpl.Effective<T, Model<T>, Model.Effective<T>> implements
				Model.Effective<T> {
			private final Set<Model.Effective<? super T>> baseModel;

			protected Effective(
					OverrideMerge<Model<T>, ModelConfiguratorImpl<T>> overrideMerge) {
				super(overrideMerge);

				Set<Model.Effective<? super T>> baseModel = new TreeSet<>(
						new IdentityComparator<>());
				overrideMerge.configurator().getOverriddenNodes()
						.forEach(n -> baseModel.addAll(n.effective().baseModel()));
				baseModel.addAll(overrideMerge.node().baseModel().stream()
						.map(SchemaNode::effective).collect(Collectors.toSet()));
				this.baseModel = Collections.unmodifiableSet(baseModel);
			}

			@Override
			public final Set<Model.Effective<? super T>> baseModel() {
				return baseModel;
			}
		}

		private final Effective<T> effective;

		private final Set<Model<? super T>> baseModel;

		public ModelImpl(ModelConfiguratorImpl<T> configurator) {
			super(configurator);

			baseModel = configurator.baseModel == null ? Collections.emptySet()
					: new HashSet<>(configurator.baseModel);

			effective = new Effective<>(overrideMerge(this, configurator));
		}

		@Override
		public final Set<Model<? super T>> baseModel() {
			return baseModel;
		}

		@Override
		public Effective<T> effective() {
			return effective;
		}
	}

	private final DataLoader loader;

	private Set<Model<? super T>> baseModel;

	public ModelConfiguratorImpl(DataLoader loader) {
		this.loader = loader;
	}

	@Override
	protected DataLoader getDataLoader() {
		return loader;
	}

	@Override
	protected Namespace getNamespace() {
		return getName().getNamespace();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ModelConfigurator<V> baseModel(
			Set<? extends Model<? super V>> base) {
		requireConfigurable(this.baseModel);
		baseModel = new HashSet<>((Set<? extends Model<? super T>>) base);

		return (ModelConfigurator<V>) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public LinkedHashSet<Model<T>> getOverriddenNodes() {
		return baseModel != null ? new LinkedHashSet<>(baseModel.stream()
				.map(m -> (Model<T>) m.effective()).collect(Collectors.toList()))
				: new LinkedHashSet<>();
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
}
