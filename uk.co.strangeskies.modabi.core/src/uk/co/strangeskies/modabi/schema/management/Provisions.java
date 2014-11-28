package uk.co.strangeskies.modabi.schema.management;

import uk.co.strangeskies.modabi.schema.TypeLiteral;

public interface Provisions {
	<T> T provide(TypeLiteral<T> type);

	default <T> T provide(Class<T> clazz) {
		return (T) provide(new TypeLiteral<>(clazz));
	}

	boolean isProvided(TypeLiteral<?> type);

	default boolean isProvided(Class<?> clazz) {
		return isProvided(new TypeLiteral<>(clazz));
	}
}
