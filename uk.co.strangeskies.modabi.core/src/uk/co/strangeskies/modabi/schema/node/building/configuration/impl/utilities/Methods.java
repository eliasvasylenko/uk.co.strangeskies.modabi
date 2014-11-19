package uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ClassUtils;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.BindingNode;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.DataNode;

public class Methods {
	private Methods() {
	}

	public static Constructor<?> findConstructor(Class<?> receiver,
			Class<?>... parameters) throws NoSuchMethodException {
		return findConstructor(receiver, Arrays.asList(parameters));
	}

	public static Constructor<?> findConstructor(Class<?> receiver,
			List<Class<?>> parameters) throws NoSuchMethodException {
		Set<Constructor<?>> overloadCandidates = findOverloadCandidates(receiver,
				Class::getConstructors, c -> true, parameters);

		if (overloadCandidates.isEmpty())
			throw new SchemaException("Cannot find constructor for class '"
					+ receiver + "' with parameters '" + parameters);

		return findMostSpecificOverload(overloadCandidates);
	}

	public static Method findMethod(List<String> names, Class<?> receiver,
			boolean isStatic, Class<?> result, boolean allowCast,
			Class<?>... parameters) throws NoSuchMethodException {
		return findMethod(names, receiver, isStatic, result, allowCast,
				Arrays.asList(parameters));
	}

	public static Method findMethod(List<String> names, Class<?> receiver,
			boolean isStatic, Class<?> result, boolean allowCast,
			List<Class<?>> parameters) throws NoSuchMethodException {
		Set<Method> overloadCandidates = findOverloadCandidates(receiver,
				Class::getMethods, c -> names.contains(c.getName()), parameters);

		if (overloadCandidates.isEmpty())
			throw new NoSuchMethodException("Cannot find method of class '" + result
					+ "', receiver '" + receiver + "', and parameters '" + parameters
					+ "' with any name of '" + names + "'.");

		Method mostSpecific = findMostSpecificOverload(overloadCandidates);

		if (result != null
				&& !ClassUtils.isAssignable(mostSpecific.getReturnType(), result)
				&& !(ClassUtils.isAssignable(result, mostSpecific.getReturnType()) && allowCast))
			throw new NoSuchMethodException("Resolved method '" + mostSpecific
					+ "' does not have compatible return type with '" + result + "'.");

		if (Modifier.isStatic(mostSpecific.getModifiers()) != isStatic)
			throw new NoSuchMethodException("Resolved method '" + mostSpecific
					+ "' should not be static.");

		return mostSpecific;
	}

	private static <I extends Executable> Set<I> findOverloadCandidates(
			Class<?> receiver, Function<Class<?>, I[]> methods, Predicate<I> filter,
			List<Class<?>> parameters) {
		return Stream
				.concat(Arrays.stream(methods.apply(receiver)),
						Arrays.stream(methods.apply(Object.class))).filter(m -> {
					if (!filter.test(m))
						return false;

					Class<?>[] methodParameters = m.getParameterTypes();

					if (methodParameters.length != parameters.size())
						return false;

					int i = 0;
					for (Class<?> parameter : parameters)
						if (!ClassUtils.isAssignable(parameter, methodParameters[i++]))
							return false;

					return true;
				}).collect(Collectors.toSet());
	}

	private static <I extends Executable> I findMostSpecificOverload(
			Set<I> candidates) throws NoSuchMethodException {
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
}
