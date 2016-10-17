package uk.co.strangeskies.modabi.schema;

import static uk.co.strangeskies.reflection.TypeToken.overAnnotatedType;
import static uk.co.strangeskies.reflection.TypeToken.overType;

import java.lang.reflect.AnnotatedType;

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
	<V> ModelConfigurator<V> baseModel(Model<? extends V> dataType);

	@Override
	Model<T> create();
}
