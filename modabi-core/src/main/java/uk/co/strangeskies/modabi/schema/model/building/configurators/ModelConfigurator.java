package uk.co.strangeskies.modabi.schema.model.building.configurators;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import uk.co.strangeskies.modabi.schema.model.Model;

public interface ModelConfigurator<T> extends
		AbstractModelConfigurator<ModelConfigurator<T>, Model<T>, T> {
	@Override
	default <V extends T> ModelConfigurator<V> baseModel(
			@SuppressWarnings("unchecked") Model<? super V>... baseModel) {
		return baseModel(new HashSet<>(Arrays.asList(baseModel)));
	}

	@Override
	<V extends T> ModelConfigurator<V> baseModel(
			Set<? extends Model<? super V>> baseModel);

	@Override
	<V extends T> ModelConfigurator<V> dataClass(Class<V> bindingClass);
}
