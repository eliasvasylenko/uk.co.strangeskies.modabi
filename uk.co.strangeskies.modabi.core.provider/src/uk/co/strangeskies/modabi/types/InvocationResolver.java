package uk.co.strangeskies.modabi.types;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;

public class InvocationResolver<T> {
	private final Type receiverType;

	private InvocationResolver(Type receiverType) {
		this.receiverType = receiverType;
	}

	public static <T> InvocationResolver<T> over(TypeToken<T> receiverType) {
		return new InvocationResolver<>(receiverType.getType());
	}

	public static <T> InvocationResolver<T> over(Class<T> receiverType) {
		if (receiverType.getTypeParameters().length > 0)
			throw new IllegalArgumentException(
					"Cannot resolve invocations over raw type '" + receiverType + "'.");
		return new InvocationResolver<>(receiverType);
	}

	public static InvocationResolver<?> over(Type receiverType) {
		boolean fullyResolved = true; // TODO verify we reference no TypeVariables
		if (!fullyResolved)
			throw new IllegalArgumentException(
					"Cannot resolve invocations over partially resolved type '"
							+ receiverType + "'.");

		return new InvocationResolver<>(receiverType);
	}

	public List<Type> inferTypes(Executable executable, Type result,
			Type... parameters) {
		return inferTypes(
				(Invokable<?, ?>) (executable instanceof Method ? Invokable.from((Method) executable)
						: Invokable.from((Constructor<?>) executable)), result,
				Arrays.asList(parameters));
	}

	public <R> List<TypeToken<?>> inferTypes(
			Invokable<? super T, ? super R> invokable, TypeToken<R> result,
			TypeToken<?>... parameters) {
		return inferTypes(
				invokable,
				result.getType(),
				Arrays.asList(parameters).stream().map(TypeToken::getType)
						.collect(Collectors.toList())).stream().map(TypeToken::of)
				.collect(Collectors.toList());
	}

	private List<Type> inferTypes(Invokable<?, ?> invokable, Type result,
			List<Type> parameters) {
		return null;
	}

	public Method resolveOverload(String name, Type result, Type... parameters) {
		return null;
	}

	public <R> Invokable<? super T, ? extends R> resolveOverload(String name,
			TypeToken<R> result, TypeToken<?>... parameters) {
		return null;
	}

	public boolean validateParameterization(Executable executable,
			Type... typeArguments) {
		return false;
	}

	public Object invokeWithParameterization(Executable executable,
			Type[] typeArguments, T receiver, Object... parameters) {
		return null;
	}

	public <R> R invokeWithParameterization(Invokable<T, R> executable,
			List<TypeToken<?>> typeArguments, T receiver, Object... parameters) {
		return null;
	}

	public Object invokeSafely(Executable executable, T receiver,
			Object... parameters) {
		return null;
	}

	public <R> R invokeSafely(Invokable<T, R> invokable, T receiver,
			Object... parameters) {
		return null;
	}
}
