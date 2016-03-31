/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.api.
 *
 * uk.co.strangeskies.modabi.core.api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.api.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypedObject;

public interface Provisions extends Scoped<Provisions> {
	default <T> void registerProvider(TypeToken<T> providedClass, Supplier<T> provider) {
		registerProvider(providedClass, s -> provider.get());
	}

	default void registerProvider(Function<TypeToken<?>, ?> provider) {
		registerProvider((t, s) -> provider.apply(t));
	}

	default <T> void registerProvider(Class<T> providedClass, Supplier<T> provider) {
		registerProvider(TypeToken.over(providedClass), provider);
	}

	<T> void registerProvider(TypeToken<T> providedClass, Function<ProcessingContext, T> provider);

	void registerProvider(BiFunction<TypeToken<?>, ProcessingContext, ?> provider);

	default <T> void registerProvider(Class<T> providedClass, Function<ProcessingContext, T> provider) {
		registerProvider(TypeToken.over(providedClass), provider);
	}

	<T> TypedObject<T> provide(TypeToken<T> type, ProcessingContext state);

	default <T> TypedObject<T> provide(Class<T> clazz, ProcessingContext state) {
		return provide(TypeToken.over(clazz), state);
	}

	boolean isProvided(TypeToken<?> type, ProcessingContext state);

	default boolean isProvided(Class<?> clazz, ProcessingContext state) {
		return isProvided(TypeToken.over(clazz), state);
	}
}
