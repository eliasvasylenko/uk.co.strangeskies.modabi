package uk.co.strangeskies.modabi.model.building.configurators.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import uk.co.strangeskies.gears.utilities.IdentityComparator;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.building.DataLoader;
import uk.co.strangeskies.modabi.model.building.configurators.ModelConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;

public class ModelConfiguratorImpl<T>
		extends
		BindingNodeConfiguratorImpl<ModelConfigurator<T>, Model<T>, T, ChildNode<?>, BindingChildNode<?, ?>>
		implements ModelConfigurator<T> {
	protected static class ModelImpl<T> extends
			BindingNodeImpl<T, Model.Effective<T>> implements Model<T> {
		private static class Effective<T> extends
				BindingNodeImpl.Effective<T, Model.Effective<T>> implements
				Model.Effective<T> {
			private final Model<T> source;

			private final Set<Model.Effective<? super T>> baseModel;
			private final Boolean isAbstract;

			protected Effective(
					OverrideMerge<Model<T>, ModelConfiguratorImpl<T>> overrideMerge) {
				super(overrideMerge);

				this.source = overrideMerge.node();

				Set<Model.Effective<? super T>> baseModel = new TreeSet<>(
						new IdentityComparator<>());
				overrideMerge.configurator().getOverriddenNodes()
						.forEach(n -> baseModel.addAll(n.effective().baseModel()));
				baseModel.addAll(overrideMerge.node().baseModel().stream()
						.map(SchemaNode::effective).collect(Collectors.toSet()));
				this.baseModel = Collections.unmodifiableSet(baseModel);

				isAbstract = overrideMerge.getValue(Model::isAbstract);
			}

			@Override
			public Model<T> source() {
				return source;
			}

			@Override
			public final Boolean isAbstract() {
				return isAbstract;
			}

			@Override
			public final Set<Model.Effective<? super T>> baseModel() {
				return baseModel;
			}
		}

		private final Effective<T> effective;

		private final Set<Model<? super T>> baseModel;
		private final Boolean isAbstract;

		public ModelImpl(ModelConfiguratorImpl<T> configurator) {
			super(configurator);

			baseModel = configurator.baseModel == null ? Collections.emptySet()
					: new HashSet<>(configurator.baseModel);
			isAbstract = configurator.isAbstract;

			effective = new Effective<>(overrideMerge(this, configurator));
		}

		@Override
		public final Boolean isAbstract() {
			return isAbstract;
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
		baseModel = new HashSet<>(Arrays.asList((Model<T>[]) base));

		return (ModelConfigurator<V>) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Set<Model<T>> getOverriddenNodes() {
		return baseModel != null ? baseModel.stream()
				.map(m -> (Model<T>) m.effective()).collect(Collectors.toSet())
				: Collections.emptySet();
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
	protected boolean isAbstract() {
		return (isAbstract != null && isAbstract)
				|| getOverriddenNodes().stream().anyMatch(
						m -> m.effective().isAbstract() != null
								&& m.effective().isAbstract());
	}
}
