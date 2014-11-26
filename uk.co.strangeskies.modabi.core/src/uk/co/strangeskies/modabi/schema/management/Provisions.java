package uk.co.strangeskies.modabi.schema.management;

import java.lang.reflect.Type;

public interface Provisions {
	Object provide(Type type);

	@SuppressWarnings("unchecked")
	default <T> T provide(Class<T> clazz) {
		return (T) provide((Type) clazz);
	}

	boolean isProvided(Type type);

	default boolean isProvided(Class<?> clazz) {
		return isProvided((Type) clazz);
	}
}
