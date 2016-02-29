/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.provider.
 *
 * uk.co.strangeskies.modabi.core.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import uk.co.strangeskies.modabi.Provisions;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypedObject;

final class ProvisionsImpl implements Provisions {
	private final List<BiFunction<TypeToken<?>, ProcessingContext, Object>> providers = new ArrayList<>();

	@Override
	public <T> void registerProvider(TypeToken<T> providedType, Function<ProcessingContext, T> provider) {
		registerProvider((c, s) -> canEqual(c, providedType) ? provider.apply(s) : null);
	}

	private boolean canEqual(TypeToken<?> first, TypeToken<?> second) {
		try {
			first.withEquality(second);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void registerProvider(BiFunction<TypeToken<?>, ProcessingContext, ?> provider) {
		providers.add((c, s) -> {
			Object provided = provider.apply(c, s);
			if (provided != null && !c.isAssignableFrom(provided.getClass()))
				throw new SchemaException("Invalid object provided for the class [" + c + "] by provider [" + provider + "]");
			return provided;
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> TypedObject<T> provide(TypeToken<T> type, ProcessingContext state) {
		return new TypedObject<>(type, (T) providers.stream().map(p -> p.apply(type, state)).filter(Objects::nonNull)
				.findFirst().orElseThrow(() -> new SchemaException("No provider exists for the type '" + type + "'")));
	}

	@Override
	public boolean isProvided(TypeToken<?> type, ProcessingContext state) {
		return providers.stream().map(p -> p.apply(type, state)).anyMatch(Objects::nonNull);
	}
}
