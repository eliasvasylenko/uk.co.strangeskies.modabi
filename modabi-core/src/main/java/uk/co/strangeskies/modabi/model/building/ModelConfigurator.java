package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.modabi.model.Model;

public interface ModelConfigurator<T> extends
		AbstractModelConfigurator<ModelConfigurator<T>, Model<T>, T> {
	@Override
	public <V extends T> ModelConfigurator<V> baseModel(
			@SuppressWarnings("unchecked") Model<? super V>... baseModel);

	@Override
	public <V extends T> ModelConfigurator<V> dataClass(Class<V> bindingClass);
}
