package uk.co.strangeskies.modabi.schema.model.building.configurators;

import uk.co.strangeskies.modabi.schema.model.Model;

public interface ModelConfigurator<T> extends
		AbstractModelConfigurator<ModelConfigurator<T>, Model<T>, T> {
	@Override
	public <V extends T> ModelConfigurator<V> baseModel(
			@SuppressWarnings("unchecked") Model<? super V>... baseModel);

	@Override
	public <V extends T> ModelConfigurator<V> dataClass(Class<V> bindingClass);
}
