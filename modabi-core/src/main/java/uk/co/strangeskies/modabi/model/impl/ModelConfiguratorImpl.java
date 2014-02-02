package uk.co.strangeskies.modabi.model.impl;

import uk.co.strangeskies.modabi.model.EffectiveModel;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.building.ModelConfigurator;

class ModelConfiguratorImpl<T> extends
		AbstractModelConfiguratorImpl<ModelConfigurator<T>, Model<T>, T> implements
		ModelConfigurator<T> {
	protected static class ModelImpl<T> extends AbstractModelImpl<T> implements
			Model<T> {
		public ModelImpl(ModelConfiguratorImpl<T> configurator) {
			super(configurator);
		}

		@Override
		public EffectiveModel<T> effectiveModel() {
			return null;
		}
	}

	public ModelConfiguratorImpl(BranchingNodeConfiguratorImpl<?, ?> parent) {
		super(parent);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ModelConfiguratorImpl<V> dataClass(Class<V> dataClass) {
		return (ModelConfiguratorImpl<V>) super.dataClass(dataClass);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ModelConfiguratorImpl<V> baseModel(
			Model<? super V>... base) {
		return (ModelConfiguratorImpl<V>) super.baseModel(base);
	}

	@Override
	public Model<T> tryCreate() {
		return new ModelImpl<>(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<Model<T>> getNodeClass() {
		return (Class<Model<T>>) (Object) Model.class;
	}
}
