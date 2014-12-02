package uk.co.strangeskies.modabi.types;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;

public class InvokationResolver {
	public Type[] inferTypes(Executable executable, Type result,
			Type... parameters) {
		return null;
	}

	public <R> List<TypeToken<?>> inferTypes(Invokable<?, ? super R> invokable,
			TypeToken<R> result, TypeToken<?>... parameters) {
		return null;
	}

	public Method resolveOverload(String name, Type receiver, Type result,
			Type... parameters) {
		return null;
	}

	public <T, R> Invokable<? super T, ? extends R> resolveOverload(String name,
			TypeToken<T> receiver, TypeToken<R> result, TypeToken<?>... parameters) {
		return null;
	}

	public boolean validateParameterization(Executable executable,
			Type... typeArguments) {
		return false;
	}

	public Object invokeWithParameterization(Executable executable,
			Type[] typeArguments, Object receiver, Object... parameters) {
		return null;
	}

	public <T, R> R invokeWithParameterization(Invokable<T, R> executable,
			List<TypeToken<?>> typeArguments, T receiver, Object... parameters) {
		return null;
	}

	public Object invokeSafely(Executable executable, Object receiver,
			Object... parameters) {
		return null;
	}

	public <T, R> R invokeSafely(Invokable<T, R> invokable, T receiver,
			Object... parameters) {
		return null;
	}
}
