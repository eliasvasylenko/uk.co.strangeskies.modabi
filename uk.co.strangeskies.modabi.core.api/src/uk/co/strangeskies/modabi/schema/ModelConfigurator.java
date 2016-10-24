package uk.co.strangeskies.modabi.schema;

import static java.util.Arrays.asList;
import static uk.co.strangeskies.reflection.TypeToken.overAnnotatedType;
import static uk.co.strangeskies.reflection.TypeToken.overType;

import java.lang.reflect.AnnotatedType;
import java.util.Collection;

import uk.co.strangeskies.reflection.TypeToken;

public interface ModelConfigurator<T> extends BindingPointConfigurator<T, ModelConfigurator<T>> {
	@Override
	default <V> ModelConfigurator<V> dataType(Class<V> dataType) {
		return dataType(overType(dataType));
	}

	@Override
	<V> ModelConfigurator<V> dataType(TypeToken<? extends V> dataType);

	@Override
	default ModelConfigurator<?> dataType(AnnotatedType dataType) {
		return dataType(overAnnotatedType(dataType));
	}

	@Override
	default <V> ModelConfigurator<V> baseModel(Model<? extends V> baseModel) {
		return baseModel(asList(baseModel));
	}

	@Override
	<V> ModelConfigurator<V> baseModel(Collection<? extends Model<? extends V>> baseModel);

	@Override
	Model<T> create();
}
