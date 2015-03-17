package uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.reflection.Invokable;
import uk.co.strangeskies.reflection.TypeLiteral;

public class Methods {
	public static <T> Invokable<T, ?> findConstructor(TypeLiteral<T> receiver,
			TypeLiteral<?>... parameters) throws NoSuchMethodException {
		return findConstructor(receiver, Arrays.asList(parameters));
	}

	public static <T> Invokable<T, ? extends T> findConstructor(
			TypeLiteral<T> receiver, List<TypeLiteral<?>> parameters)
			throws NoSuchMethodException {
		Invokable<T, ? extends T> constructor;
		try {
			constructor = receiver.resolveConstructorOverload(parameters.stream()
					.map(TypeLiteral::getType).collect(Collectors.toList()));
		} catch (Exception e) {
			throw new SchemaException("Cannot find constructor for class '"
					+ receiver + "' with parameters '" + parameters + "'.", e);
		}

		return constructor;
	}

	public static <T> Invokable<T, ?> findMethod(List<String> names,
			TypeLiteral<T> receiver, boolean isStatic, TypeLiteral<?> result,
			boolean allowCast, TypeLiteral<?>... parameters)
			throws NoSuchMethodException {
		return findMethod(names, receiver, isStatic, result, allowCast,
				Arrays.asList(parameters));
	}

	public static <T> Invokable<T, ?> findMethod(List<String> names,
			TypeLiteral<T> receiver, boolean isStatic, TypeLiteral<?> result,
			boolean allowCast, List<TypeLiteral<?>> parameters)
			throws NoSuchMethodException {
		Invokable<T, ?> method = null;

		Exception exception = null;
		for (String name : names) {
			try {
				new TypeLiteral<SchemaNode.Effective<?, ?>>() {}
						.resolveSupertypeParameters(SchemaNode.class);

				method = receiver.resolveMethodOverload(
						name,
						parameters.stream().map(TypeLiteral::getType)
								.collect(Collectors.toList()));
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
