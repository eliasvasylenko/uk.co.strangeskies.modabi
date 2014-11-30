package uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.schema.SchemaException;

import com.google.common.reflect.Invokable;
import com.google.common.reflect.Parameter;
import com.google.common.reflect.TypeToken;

public class Methods {
	public static <T> Constructor<T> findConstructor(TypeToken<T> receiver,
			TypeToken<?>... parameters) throws NoSuchMethodException {
		return findConstructor(receiver, Arrays.asList(parameters));
	}

	public static <T> Constructor<T> findConstructor(TypeToken<T> receiver,
			List<TypeToken<?>> parameters) throws NoSuchMethodException {
		@SuppressWarnings("unchecked")
		Map<Invokable<T, ?>, Constructor<T>> constructors = Arrays.stream(
				receiver.getRawType().getConstructors()).collect(
				Collectors.toMap(receiver::constructor, c -> (Constructor<T>) c));

		Set<Invokable<T, ?>> overloadCandidates = findOverloadCandidates(
				constructors.keySet(), parameters);

		if (overloadCandidates.isEmpty())
			throw new SchemaException("Cannot find constructor for class '"
					+ receiver + "' with parameters '" + parameters + "'.");

		return constructors.get(findMostSpecificOverload(overloadCandidates));
	}

	public static Method findMethod(List<String> names, TypeToken<?> receiver,
			boolean isStatic, TypeToken<?> result, boolean allowCast,
			TypeToken<?>... parameters) throws NoSuchMethodException {
		return findMethod(names, receiver, isStatic, result, allowCast,
				Arrays.asList(parameters));
	}

	public static <T> Method findMethod(List<String> names,
			TypeToken<T> receiver, boolean isStatic, TypeToken<?> result,
			boolean allowCast, List<TypeToken<?>> parameters)
			throws NoSuchMethodException {
		@SuppressWarnings("serial")
		Map<Invokable<T, ?>, Method> methods = Stream
				.concat(
						Arrays.stream(new TypeToken<Object>() {}.getRawType().getMethods()),
						Arrays.stream(receiver.getRawType().getMethods()))
				.filter(m -> names.contains(m.getName()))
				.collect(Collectors.toMap(receiver::method, Function.identity()));

		Set<Invokable<T, ?>> overloadCandidates = findOverloadCandidates(
				methods.keySet(), parameters);

		if (overloadCandidates.isEmpty())
			throw new NoSuchMethodException("Cannot find method of class '" + result
					+ "', receiver '" + receiver + "', and parameters '" + parameters
					+ "' with any name of '" + names + "'.");

		Invokable<T, ?> mostSpecific = findMostSpecificOverload(overloadCandidates);

		if (result != null && !isAssignable(mostSpecific.getReturnType(), result)
				&& !(isAssignable(result, mostSpecific.getReturnType()) && allowCast))
			throw new NoSuchMethodException("Resolved method '" + mostSpecific
					+ "' does not have compatible return type with '" + result + "'.");

		if (Modifier.isStatic(mostSpecific.getModifiers()) != isStatic)
			throw new NoSuchMethodException("Resolved method '" + mostSpecific
					+ "' should not be static.");

		return methods.get(mostSpecific);
	}

	private static <T> Set<Invokable<T, ?>> findOverloadCandidates(
			Collection<? extends Invokable<T, ?>> methods,
			List<TypeToken<?>> parameters) {
		return methods
				.stream()
				.filter(m -> m.getParameters().size() == parameters.size())
				.filter(
						m -> {
							List<TypeToken<?>> methodParameters = m.getParameters().stream()
									.map(Parameter::getType).collect(Collectors.toList());

							int i = 0;
							for (TypeToken<?> parameter : parameters)
								if (!isAssignable(methodParameters.get(i++), parameter))
									return false;

							return true;
						}).collect(Collectors.toSet());
	}

	// TODO treat without boxing/unboxing conversions as more specific
	private static <T> Invokable<T, ?> findMostSpecificOverload(
			Collection<? extends Invokable<T, ?>> candidates)
			throws NoSuchMethodException {
		if (candidates.size() == 1)
			return candidates.iterator().next();

		/*
		 * Find which candidates have the joint most specific parameters, one
		 * parameter at a time.
		 */
		Set<Invokable<T, ?>> mostSpecificSoFar = new HashSet<>(candidates);
		int parameters = candidates.iterator().next().getParameters().size();
		for (int i = 0; i < parameters; i++) {
			Set<Invokable<T, ?>> mostSpecificForParameter = new HashSet<>();

			TypeToken<?> mostSpecificParameterType = candidates.iterator().next()
					.getParameters().get(i).getType();

			for (Invokable<T, ?> overloadCandidate : candidates) {
				TypeToken<?> parameterClass = overloadCandidate.getParameters().get(i)
						.getType();

				if (isAssignable(parameterClass, mostSpecificParameterType)) {
					mostSpecificParameterType = parameterClass;

					if (!isAssignable(mostSpecificParameterType, parameterClass))
						mostSpecificForParameter.clear();
					mostSpecificForParameter.add(overloadCandidate);
				} else if (!isAssignable(mostSpecificParameterType, parameterClass)) {
					throw new NoSuchMethodException("Cannot resolve method ambiguity.");
				}
			}

			mostSpecificSoFar.retainAll(mostSpecificForParameter);

			if (mostSpecificSoFar.isEmpty())
				throw new NoSuchMethodException("Cannot resolve method ambiguity.");
		}

		/*
		 * Find which of the remaining candidates, which should all have identical
		 * parameter types, is declared in the lowermost class.
		 */
		Iterator<Invokable<T, ?>> overrideCandidateIterator = mostSpecificSoFar
				.iterator();
		Invokable<T, ?> mostSpecific = overrideCandidateIterator.next();
		while (overrideCandidateIterator.hasNext()) {
			Invokable<T, ?> candidate = overrideCandidateIterator.next();
			mostSpecific = candidate.getDeclaringClass().isAssignableFrom(
					mostSpecific.getDeclaringClass()) ? candidate : mostSpecific;
		}

		return mostSpecific;
	}

	private static Set<TypeToken<?>> getBounds(TypeToken<?> type) {
		if (type.getType() instanceof TypeVariable)
			return Arrays.asList(((TypeVariable<?>) type.getType()).getBounds())
					.stream().map(TypeToken::of).collect(Collectors.toSet());
		else {
			Set<TypeToken<?>> types = new HashSet<>();
			types.add(type);
			return types;
		}
	}

	private static boolean isAssignable(TypeToken<?> target, TypeToken<?> value) {
		Set<TypeToken<?>> targets = getBounds(target);
		Set<TypeToken<?>> values = getBounds(value);

		System.out.println("targets: " + targets);
		System.out.println("values: " + values);

		boolean assignable = values.stream().allMatch(
				v -> targets.stream()
						.anyMatch(t -> t.wrap().isAssignableFrom(v.wrap())));

		System.out.println(" = assignable: " + assignable);

		return assignable;
	}
}
