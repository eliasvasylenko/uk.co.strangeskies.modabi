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

import java.lang.reflect.Executable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.lang.model.type.ExecutableType;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.reflection.ExecutableMember;
import uk.co.strangeskies.reflection.TypeToken;

public class Methods {
	public static <T> ExecutableMember<? super T, ? extends T> findConstructor(TypeToken<T> receiver,
			TypeToken<?>... parameters) throws NoSuchMethodException {
		return findConstructor(receiver, Arrays.asList(parameters));
	}

	public static <T> ExecutableMember<? super T, ? extends T> findConstructor(TypeToken<T> receiver,
			List<TypeToken<?>> parameters) throws NoSuchMethodException {
		ExecutableMember<? super T, ? extends T> constructor;
		try {
			constructor = receiver.resolveConstructorOverload(parameters);
		} catch (Exception e) {
			throw new ModabiException(t -> t.noMemberFound(receiver, parameters, ExecutableType.CONSTRUCTOR), e);
		}

		return constructor;
	}

	public static <T> ExecutableMember<? super T, ?> findMethod(List<String> names, TypeToken<T> receiver,
			boolean isStatic, TypeToken<?> result, boolean allowCast, TypeToken<?>... parameters) {
		return findMethod(names, receiver, isStatic, result, allowCast, Arrays.asList(parameters));
	}

	public static <T> ExecutableMember<? super T, ?> findMethod(List<String> names, TypeToken<T> receiver,
			boolean isStatic, TypeToken<?> result, boolean allowCast, List<TypeToken<?>> parameters) {
		ExecutableMember<? super T, ?> method = null;

		try {
			method = resolveMethodOverload(receiver, names, parameters);
		} catch (Exception e) {
			ExecutableType type = isStatic ? ExecutableType.STATIC_METHOD : ExecutableType.METHOD;
			throw new ModabiException(t -> t.noMemberFound(receiver, parameters, type), e);
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

		return method;
	}

	private static <T> ExecutableMember<? super T, ?> resolveMethodOverload(TypeToken<T> type, List<String> names,
			List<? extends TypeToken<?>> arguments) {
		Set<? extends ExecutableMember<? super T, ? extends Object>> candidates = type
				.getMethods(m -> names.contains(m.getName()) && isArgumentCountValid(m, arguments.size()));

		if (candidates.isEmpty())
			throw new ModabiException(t -> t.noMethodCandidatesFoundForNames(names));

		candidates = ExecutableMember.resolveApplicableExecutableMembers(candidates, arguments);

		return ExecutableMember.resolveMostSpecificExecutableMember(candidates);
	}

	private static boolean isArgumentCountValid(Executable method, int arguments) {
		return (method.isVarArgs() ? method.getParameterCount() <= arguments + 1 : method.getParameterCount() == arguments);
	}
}
