package uk.co.strangeskies.modabi.schema;

import static java.util.Arrays.asList;
import static uk.co.strangeskies.reflection.token.TypeToken.overAnnotatedType;
import static uk.co.strangeskies.reflection.token.TypeToken.overType;

import java.lang.reflect.AnnotatedType;
import java.util.Collection;

import uk.co.strangeskies.reflection.token.TypeToken;

public interface ModelConfigurator<T> extends BindingPointConfigurator<T, ModelConfigurator<T>> {
	@Override
	default <V> ModelConfigurator<V> dataType(Class<V> dataType) {
		return dataType(overType(dataType));
	}

	@Override
	<V> ModelConfigurator<V> dataType(TypeToken<? super V> dataType);

	@Override
	default ModelConfigurator<?> dataType(AnnotatedType dataType) {
		return dataType(overAnnotatedType(dataType));
	}

	@SuppressWarnings("unchecked")
	@Override
	default <V> ModelConfigurator<V> baseModel(Model<? super V> baseModel) {
		return (ModelConfigurator<V>) baseModel(asList(baseModel));
	}

	@Override
	ModelConfigurator<?> baseModel(Collection<? extends Model<?>> baseModel);

	@Override
	Model<T> create();
}
