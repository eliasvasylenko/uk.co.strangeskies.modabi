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

import java.util.function.Function;
import java.util.function.Supplier;

import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface Provider {
	<T> T provide(TypeToken<T> requestedType, ProcessingContext context);

	static <T> Provider over(TypeToken<T> providedType, Supplier<T> provider) {
		return over(providedType, s -> provider.get());
	}

	static <T> Provider over(Class<T> providedType, Supplier<T> provider) {
		return over(TypeToken.overType(providedType), provider);
	}

	static <T> Provider over(TypeToken<T> providedType, Function<ProcessingContext, T> provider) {
		return new Provider() {
			@SuppressWarnings("unchecked")
			@Override
			public <U> U provide(TypeToken<U> requestedType, ProcessingContext context) {
				return Provider.canEqual(requestedType, providedType) ? (U) provider.apply(context) : null;
			}
		};
	}

	static <T> Provider over(Class<T> providedClass, Function<ProcessingContext, T> provider) {
		return over(TypeToken.overType(providedClass), provider);
	}

	static boolean canEqual(TypeToken<?> first, TypeToken<?> second) {
		try {
			first.withConstraintFrom(Kind.EQUALITY, second);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
