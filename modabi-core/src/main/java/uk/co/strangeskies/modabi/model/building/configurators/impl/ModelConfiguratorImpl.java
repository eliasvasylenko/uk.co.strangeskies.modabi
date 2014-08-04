package uk.co.strangeskies.modabi.model.building.configurators.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.building.DataLoader;
import uk.co.strangeskies.modabi.model.building.configurators.ModelConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;

public class ModelConfiguratorImpl<T>
		extends
		BindingNodeConfiguratorImpl<ModelConfigurator<T>, Model<T>, T, ChildNode<?>, BindingChildNode<?, ?>>
		implements ModelConfigurator<T> {
	protected static class ModelImpl<T> extends
			BindingNodeImpl<T, Model.Effective<T>> implements Model<T> {
		private static class Effective<T> extends
				BindingNodeImpl.Effective<T, Model.Effective<T>> implements
				Model.Effective<T> {
			private final List<Model.Effective<? super T>> baseModel;
			private final Boolean isAbstract;

			protected Effective(
					OverrideMerge<Model<T>, ModelConfiguratorImpl<T>> overrideMerge) {
				super(overrideMerge);

				baseModel = new ArrayList<>();
				overrideMerge
						.configurator()
						.getOverriddenNodes()
						.forEach(
								n -> baseModel.addAll(n.baseModel().stream()
										.map(m -> m.effective()).collect(Collectors.toList())));
				baseModel.addAll(overrideMerge.node().baseModel().stream()
						.map(m -> m.effective()).collect(Collectors.toList()));

				isAbstract = overrideMerge.getValue(Model::isAbstract);
			}

			@Override
			public final Boolean isAbstract() {
				return isAbstract;
			}

			@Override
			public final List<Model.Effective<? super T>> baseModel() {
				return baseModel;
			}
		}

		private final Effective<T> effective;

		private final List<Model<? super T>> baseModel;
		private final Boolean isAbstract;

		public ModelImpl(ModelConfiguratorImpl<T> configurator) {
			super(configurator);

			baseModel = configurator.baseModel == null ? new ArrayList<>()
					: new ArrayList<>(configurator.baseModel);
			isAbstract = configurator.isAbstract;

			effective = new Effective<>(overrideMerge(this, configurator));
		}

		@Override
		public final Boolean isAbstract() {
			return isAbstract;
		}

		@Override
		public final List<Model<? super T>> baseModel() {
			return baseModel;
		}

		@Override
		public Effective<T> effective() {
			return effective;
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

		return (ModelConfigurator<V>) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Set<Model<T>> getOverriddenNodes() {
		return (Set<Model<T>>) (Object) baseModel;
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
						m -> m.effective().isAbstract());
	}
}
