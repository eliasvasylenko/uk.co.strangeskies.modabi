package uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ClassUtils;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.BindingNode;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.DataNode;

import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;

public class Methods {
	public static Constructor<?> findConstructor(TypeToken<?> receiver,
			TypeToken<?>... parameters) throws NoSuchMethodException {
		return findConstructor(receiver, Arrays.asList(parameters));
	}

	public static Constructor<?> findConstructor(TypeToken<?> receiver,
			List<TypeToken<?>> parameters) throws NoSuchMethodException {
		Set<Constructor<?>> overloadCandidates = findOverloadCandidates(receiver,
				c -> Arrays.asList(c.getRawType().getConstructors()), parameters);

		if (overloadCandidates.isEmpty())
			throw new SchemaException("Cannot find constructor for class '"
					+ receiver + "' with parameters '" + parameters);

		return findMostSpecificOverload(receiver, overloadCandidates,
				receiver::constructor);
	}

	public static Method findMethod(List<String> names, TypeToken<?> receiver,
			boolean isStatic, TypeToken<?> result, boolean allowCast,
			TypeToken<?>... parameters) throws NoSuchMethodException {
		return findMethod(names, receiver, isStatic, result, allowCast,
				Arrays.asList(parameters));
	}

	public static Method findMethod(List<String> names, TypeToken<?> receiver,
			boolean isStatic, TypeToken<?> result, boolean allowCast,
			List<TypeToken<?>> parameters) throws NoSuchMethodException {
		Set<Method> overloadCandidates = findOverloadCandidates(
				receiver,
				c -> Arrays.stream(c.getRawType().getMethods())
						.filter(m -> names.contains(m.getName()))
						.collect(Collectors.toList()), parameters);

		if (overloadCandidates.isEmpty())
			throw new NoSuchMethodException("Cannot find method of class '" + result
					+ "', receiver '" + receiver + "', and parameters '" + parameters
					+ "' with any name of '" + names + "'.");

		Method mostSpecific = findMostSpecificOverload(receiver,
				overloadCandidates, receiver::method);

		if (result != null
				&& !isAssignable(receiver.method(mostSpecific).getReturnType(), result)
				&& !(isAssignable(result, receiver.method(mostSpecific).getReturnType()) && allowCast))
			throw new NoSuchMethodException("Resolved method '" + mostSpecific
					+ "' does not have compatible return type with '" + result + "'.");

		if (Modifier.isStatic(mostSpecific.getModifiers()) != isStatic)
			throw new NoSuchMethodException("Resolved method '" + mostSpecific
					+ "' should not be static.");

		return mostSpecific;
	}

	@SuppressWarnings("serial")
	private static <I extends Executable> Set<I> findOverloadCandidates(
			TypeToken<?> receiver,
			Function<TypeToken<?>, Collection<? extends I>> methods,
			List<TypeToken<?>> parameters) {
		return Stream
				.concat(methods.apply(receiver).stream(),
						methods.apply(new TypeToken<Object>() {
						}).stream())
				.filter(
						m -> {
							List<TypeToken<?>> methodParameters = Arrays
									.asList(m.getGenericParameterTypes()).stream()
									.map(TypeToken::of).collect(Collectors.toList());

							if (methodParameters.size() != parameters.size())
								return false;

							int i = 0;
							for (TypeToken<?> parameter : parameters)
								if (!isAssignable(parameter, methodParameters.get(i++)))
									return false;

							return true;
						}).collect(Collectors.toSet());
	}

	private static <I extends Executable> I findMostSpecificOverload(
			TypeToken<?> receiver, Set<I> candidates,
			Function<I, Invokable<?, ?>> invokable) throws NoSuchMethodException {
		if (candidates.size() == 1)
			return candidates.iterator().next();

		/*
		 * Find which candidates have the joint most specific parameters, one
		 * parameter at a time.
		 */
		Set<I> mostSpecificSoFar = new HashSet<>(candidates);
		int parameters = candidates.iterator().next().getParameterCount();
		for (int i = 0; i < parameters; i++) {
			Set<I> mostSpecificForParameter = new HashSet<>();

			Class<?> mostSpecificParameterClass = candidates.iterator().next()
					.getDeclaringClass();

			for (I overloadCandidate : candidates) {
				Class<?> parameterClass = overloadCandidate.getDeclaringClass();

				if (ClassUtils.isAssignable(parameterClass, mostSpecificParameterClass)) {
					mostSpecificParameterClass = parameterClass;

					if (!ClassUtils.isAssignable(mostSpecificParameterClass,
							parameterClass))
						mostSpecificForParameter.clear();
					mostSpecificForParameter.add(overloadCandidate);
				} else if (!ClassUtils.isAssignable(mostSpecificParameterClass,
						parameterClass)) {
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
		Iterator<I> overrideCandidateIterator = mostSpecificSoFar.iterator();
		I mostSpecific = overrideCandidateIterator.next();
		while (overrideCandidateIterator.hasNext()) {
			I candidate = overrideCandidateIterator.next();
			mostSpecific = ClassUtils.isAssignable(candidate.getDeclaringClass(),
					mostSpecific.getDeclaringClass()) ? candidate : mostSpecific;
		}

		return mostSpecific;
	}

	public static List<DataNode.Effective<?>> findProvidedUnbindingParameters(
			BindingNode.Effective<?, ?, ?> node) {
		return node.getProvidedUnbindingMethodParameterNames() == null ? node
				.getUnbindingMethodName() == null ? null : new ArrayList<>()
				: node
						.getProvidedUnbindingMethodParameterNames()
						.stream()
						.map(
								p -> {
									if (p.getName().equals("this"))
										return null;
									else {
										ChildNode.Effective<?, ?> effective = node
												.children()
												.stream()
												.filter(c -> c.getName().equals(p))
												.findAny()
												.orElseThrow(
														() -> new SchemaException(
																"Cannot find node for unbinding parameter: '"
																		+ p + "'"));

										if (!(effective instanceof DataNode.Effective))
											throw new SchemaException("Unbinding parameter node '"
													+ effective + "' for '" + p + "' is not a data node.");

										DataNode.Effective<?> dataNode = (DataNode.Effective<?>) effective;

										if (dataNode.occurrences() != null
												&& (dataNode.occurrences().getTo() != 1 || dataNode
														.occurrences().getFrom() != 1))
											throw new SchemaException("Unbinding parameter node '"
													+ effective + "' for '" + p
													+ "' must occur exactly once.");

										if (!node.isAbstract() && !dataNode.isValueProvided())
											throw new SchemaException("Unbinding parameter node '"
													+ dataNode + "' for '" + p
													+ "' must provide a value.");

										return dataNode;
									}
								}).collect(Collectors.toList());
	}

	private static boolean isAssignable(TypeToken<?> target, TypeToken<?> value) {
		return target.wrap().isAssignableFrom(value.wrap());
	}
}
