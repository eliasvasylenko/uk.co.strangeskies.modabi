package uk.co.strangeskies.modabi.model.impl;

import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.building.ModelConfigurator;

public class ModelConfiguratorImpl<T> extends
		AbstractModelConfiguratorImpl<ModelConfigurator<T>, Model<T>, T> implements
		ModelConfigurator<T> {
	public ModelConfiguratorImpl() {
		super(new NodeBuilderContext());
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
		return this;
	}
}
