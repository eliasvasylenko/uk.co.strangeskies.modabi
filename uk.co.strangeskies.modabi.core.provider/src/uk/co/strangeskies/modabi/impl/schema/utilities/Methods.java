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
package uk.co.strangeskies.modabi.impl.schema.utilities;

import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.TypeToken;

public class Methods {
	public static <T> ExecutableToken<Void, T> findConstructor(TypeToken<T> receiver, TypeToken<?>... parameters) {
		return findConstructor(receiver, Arrays.asList(parameters));
	}

	public static <T> ExecutableToken<Void, T> findConstructor(TypeToken<T> receiver, List<TypeToken<?>> parameters) {
		ExecutableToken<Void, T> constructor;
		try {
			constructor = receiver.constructors().resolveOverload(parameters);
		} catch (Exception e) {
			throw new ModabiException(t -> t.noConstructorFound(receiver, parameters), e);
		}

		return constructor;
	}

	public static <T> ExecutableToken<T, ?> findMethod(List<String> names, TypeToken<T> receiver, boolean isStatic,
			TypeToken<?> result, boolean allowCast, TypeToken<?>... parameters) {
		return findMethod(names, receiver, isStatic, result, allowCast, Arrays.asList(parameters));
	}

	public static <T> ExecutableToken<T, ?> findMethod(List<String> names, TypeToken<T> receiver, boolean isStatic,
			TypeToken<?> result, boolean allowCast, List<TypeToken<?>> parameters) {
		RuntimeException cause = null;

		for (String name : names) {
			ExecutableToken<T, ?> method = null;

			try {
				try {
					method = receiver.methods().named(name).resolveOverload(parameters);
				} catch (Exception e) {
					throw new ModabiException(t -> t.noMethodFound(receiver, parameters), e);
				}

				if (result != null) {
					if (!allowCast) {
						method = method.withTargetType(result);
					} else {
						/*
						 * TODO Enforce castability, with special treatment for iterable out
						 * methods.
						 */
					}
				}
			} catch (RuntimeException e) {
				if (cause == null)
					cause = e;
			}

			if (method != null) {
				return method;
			}
		}

		throw new ModabiException(t -> t.noMethodCandidatesFoundForNames(names), cause);
	}
}
