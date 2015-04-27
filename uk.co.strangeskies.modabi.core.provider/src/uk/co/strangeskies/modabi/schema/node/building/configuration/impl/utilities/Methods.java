/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities;

import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.reflection.Invokable;
import uk.co.strangeskies.reflection.TypeToken;

public class Methods {
	public static <T> Invokable<? super T, ? extends T> findConstructor(
			TypeToken<T> receiver, TypeToken<?>... parameters)
			throws NoSuchMethodException {
		return findConstructor(receiver, Arrays.asList(parameters));
	}

	public static <T> Invokable<? super T, ? extends T> findConstructor(
			TypeToken<T> receiver, List<TypeToken<?>> parameters)
			throws NoSuchMethodException {
		Invokable<? super T, ? extends T> constructor;
		try {
			constructor = receiver.resolveConstructorOverload(parameters);
		} catch (Exception e) {
			throw new SchemaException("Cannot find constructor for class '"
					+ receiver + "' with parameters '" + parameters + "'.", e);
		}

		return constructor;
	}

	public static <T> Invokable<? super T, ?> findMethod(List<String> names,
			TypeToken<T> receiver, boolean isStatic, TypeToken<?> result,
			boolean allowCast, TypeToken<?>... parameters)
			throws NoSuchMethodException {
		return findMethod(names, receiver, isStatic, result, allowCast,
				Arrays.asList(parameters));
	}

	public static <T> Invokable<? super T, ?> findMethod(List<String> names,
			TypeToken<T> receiver, boolean isStatic, TypeToken<?> result,
			boolean allowCast, List<TypeToken<?>> parameters)
			throws NoSuchMethodException {
		Invokable<? super T, ?> method = null;

		Exception exception = null;
		for (String name : names) {
			try {
				method = receiver.resolveMethodOverload(name, parameters);
				break;
			} catch (Exception e) {
				exception = e;
			}
		}

		if (method == null)
			throw new SchemaException("Cannot find " + (isStatic ? "static " : "")
					+ "method for class '" + receiver + "' with parameters '"
					+ parameters + "' and any name of '" + names + "'.", exception);

		return method;
	}
}
