package uk.co.strangeskies.modabi.schema.node.model;

import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.modabi.schema.node.building.configuration.AbstractModelConfigurator;

public interface ModelConfigurator<T> extends
		AbstractModelConfigurator<ModelConfigurator<T>, Model<T>, T> {
	@Override
	default <V extends T> ModelConfigurator<V> baseModel(
			@SuppressWarnings("unchecked") Model<? super V>... baseModel) {
		return baseModel(Arrays.asList(baseModel));
	}

	@Override
	<V extends T> ModelConfigurator<V> baseModel(
			List<? extends Model<? super V>> baseModel);

	@Override
	<V extends T> ModelConfigurator<V> dataClass(Class<V> bindingClass);
}
