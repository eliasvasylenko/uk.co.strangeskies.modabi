package uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ClassUtils;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.BindingChildNode;
import uk.co.strangeskies.modabi.schema.node.BindingNode;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.InputNode;
import uk.co.strangeskies.modabi.schema.processing.unbinding.UnbindingStrategy;

public class Methods {
	private Methods() {
	}

	/*
	 * TODO think about whether this input node is first of a special binding
	 * type, e.g. STATIC_FACTORY, and therefore, in that example, be a static
	 * method.
	 */
	public static Method getInMethod(InputNode.Effective<?, ?> node,
			Method inheritedInMethod, Class<?> receiverClass,
			List<Class<?>> parameters) {
		try {
			Class<?> result;
			if (node.isInMethodChained()) {
				result = node.source().getPostInputClass();
				if (result == null)
					result = Object.class;
			} else
				result = null;

			return findMethod(generateInMethodNames(node), receiverClass, result,
					node != null && node.isInMethodChained() && node.isInMethodCast(),
					parameters.toArray(new Class<?>[parameters.size()]));
		} catch (NoSuchMethodException e) {
			throw new SchemaException(e);
		}
	}

	public static Method getOutMethod(BindingChildNode.Effective<?, ?, ?> node,
			Method inheritedOutMethod, Class<?> targetClass) {
		try {
			Class<?> resultClass = (node.isOutMethodIterable() != null && node
					.isOutMethodIterable()) ? Iterable.class : node.getDataClass();

			Method outMethod;
			if (node.getOutMethodName() != null
					&& node.getOutMethodName().equals("this")) {
				if (!ClassUtils.isAssignable(targetClass, resultClass))
					throw new SchemaException("Can't use out method 'this' for node '"
							+ node.getName() + "', as result class '" + resultClass
							+ "' cannot be assigned from target class'" + targetClass + "'.");
				outMethod = null;
			} else if (targetClass == null) {
				if (!node.isAbstract())
					throw new SchemaException("Can't find out method for node '"
							+ node.getName() + "' as target class cannot be found.");
				outMethod = null;
			} else if (resultClass == null) {
				if (!node.isAbstract())
					throw new SchemaException("Can't find out method for node '"
							+ node.getName() + "' as result class cannot be found.");
				outMethod = null;
			} else {
				outMethod = findMethod(generateOutMethodNames(node, resultClass),
						targetClass, resultClass, false);

				if (inheritedOutMethod != null && !outMethod.equals(inheritedOutMethod))
					throw new SchemaException();
			}

			return outMethod;
		} catch (NoSuchMethodException e) {
			throw new SchemaException(e);
		}
	}

	private static List<String> generateInMethodNames(
			InputNode.Effective<?, ?> node) {
		List<String> names;

		if (node.getInMethodName() != null)
			names = Arrays.asList(node.getInMethodName());
		else {
			names = new ArrayList<>();

			names.add(node.getName().getName());
			names.add(node.getName().getName() + "Value");

			List<String> namesAndBlank = new ArrayList<>(names);
			namesAndBlank.add("");

			for (String name : namesAndBlank) {
				names.add("set" + capitalize(name));
				names.add("from" + capitalize(name));
				names.add("parse" + capitalize(name));
				names.add("add" + capitalize(name));
				names.add("put" + capitalize(name));
			}
		}

		return names;
	}

	private static List<String> generateOutMethodNames(
			BindingChildNode.Effective<?, ?, ?> node, Class<?> resultClass) {
		List<String> names;

		if (node.getOutMethodName() != null)
			names = Arrays.asList(node.getOutMethodName());
		else
			names = generateUnbindingMethodNames(node.getName().getName(),
					node.isOutMethodIterable() != null && node.isOutMethodIterable(),
					resultClass);

		return names;
	}

	public static Method findUnbindingMethod(BindingNode.Effective<?, ?, ?> node) {
		UnbindingStrategy unbindingStrategy = node.getUnbindingStrategy();
		if (unbindingStrategy == null)
			unbindingStrategy = UnbindingStrategy.SIMPLE;

		switch (unbindingStrategy) {
		case SIMPLE:
		case CONSTRUCTOR:
			return null;

		case STATIC_FACTORY:
		case PROVIDED_FACTORY:
			Class<?> receiverClass = node.getUnbindingFactoryClass() != null ? node
					.getUnbindingFactoryClass() : node.getUnbindingClass();
			return findUnbindingMethod(node, node.getUnbindingClass(), receiverClass,
					findUnbindingMethodParameterClasses(node, BindingNode::getDataClass));

		case PASS_TO_PROVIDED:
			return findUnbindingMethod(node, null, node.getUnbindingClass(),
					findUnbindingMethodParameterClasses(node, BindingNode::getDataClass));

		case ACCEPT_PROVIDED:
			return findUnbindingMethod(
					node,
					null,
					node.getDataClass(),
					findUnbindingMethodParameterClasses(node,
							BindingNode::getUnbindingClass));
		}
		throw new AssertionError();
	}

	private static List<Class<?>> findUnbindingMethodParameterClasses(
			BindingNode.Effective<?, ?, ?> node,
			Function<BindingNode.Effective<?, ?, ?>, Class<?>> nodeClass) {
		List<Class<?>> classList = new ArrayList<>();

		boolean addedNodeClass = false;
		List<DataNode.Effective<?>> parameters = node
				.getProvidedUnbindingMethodParameters();
		if (parameters != null) {
			for (DataNode.Effective<?> parameter : parameters) {
				if (parameter == null)
					if (addedNodeClass)
						throw new SchemaException();
					else {
						addedNodeClass = true;
						classList.add(nodeClass.apply(node));
					}
				else {
					classList.add(parameter.getDataClass());
				}
			}
		}
		if (!addedNodeClass)
			classList.add(0, nodeClass.apply(node));

		return classList;
	}

	private static Method findUnbindingMethod(
			BindingNode.Effective<?, ?, ?> node, Class<?> result, Class<?> receiver,
			List<Class<?>> parameters) {
		List<String> names = generateUnbindingMethodNames(node, result);
		try {
			return findMethod(names, receiver, result, false,
					parameters.toArray(new Class<?>[] {}));
		} catch (NoSuchMethodException | SchemaException | SecurityException e) {
			throw new SchemaException("Cannot find unbinding method for node '"
					+ node + "' of class '" + result + "', reveiver '" + receiver
					+ "', and parameters '" + parameters + "' with any name of '" + names
					+ "'.", e);
		}
	}

	public static Method findMethod(List<String> names, Class<?> receiver,
			Class<?> result, boolean allowCast, Class<?>... parameters)
			throws NoSuchMethodException {
		Method method = tryFindMethod(names, receiver, result, allowCast,
				parameters);
		if (method == null)
			throw new SchemaException("Cannot find method of class '" + result
					+ "', reveiver '" + receiver + "', and parameters '"
					+ Arrays.asList(parameters) + "' with any name of '" + names + "'.");

		return method;
	}

	public static Method tryFindMethod(List<String> names, Class<?> receiver,
			Class<?> result, boolean allowCast, Class<?>... parameters)
			throws NoSuchMethodException {
		Set<Method> overloadCandidates = Stream
				.concat(Arrays.stream(receiver.getMethods()),
						Arrays.stream(Object.class.getMethods())).filter(m -> {
					if (!names.contains(m.getName()))
						return false;

					Class<?>[] methodParameters = m.getParameterTypes();

					if (methodParameters.length != parameters.length)
						return false;

					int i = 0;
					for (Class<?> parameter : parameters)
						if (!ClassUtils.isAssignable(parameter, methodParameters[i++]))
							return false;

					return true;
				}).collect(Collectors.toSet());

		if (overloadCandidates.isEmpty())
			throw new SchemaException("Cannot find method of class '" + result
					+ "', reveiver '" + receiver + "', and parameters '"
					+ Arrays.toString(parameters) + "' with any name of '" + names + "'.");

		Method mostSpecific = findMostSpecificOverload(overloadCandidates);

		if (result != null
				&& !ClassUtils.isAssignable(mostSpecific.getReturnType(), result)
				&& !(ClassUtils.isAssignable(result, mostSpecific.getReturnType()) && allowCast))
			throw new NoSuchMethodException("Resolved method '" + mostSpecific
					+ "' does not have compatible return type with '" + result + "'.");

		return mostSpecific;
	}

	private static Method findMostSpecificOverload(Set<Method> candidates)
			throws NoSuchMethodException {
		if (candidates.size() == 1)
			return candidates.iterator().next();

		/*
		 * Find which candidates have the joint most specific parameters, one
		 * parameter at a time.
		 */
		Set<Method> mostSpecificSoFar = new HashSet<>(candidates);
		int parameters = candidates.iterator().next().getParameterCount();
		for (int i = 0; i < parameters; i++) {
			Set<Method> mostSpecificForParameter = new HashSet<>();

			Class<?> mostSpecificParameterClass = candidates.iterator().next()
					.getDeclaringClass();

			for (Method overloadCandidate : candidates) {
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
		Iterator<Method> overrideCandidateIterator = mostSpecificSoFar.iterator();
		Method mostSpecific = overrideCandidateIterator.next();
		while (overrideCandidateIterator.hasNext()) {
			Method candidate = overrideCandidateIterator.next();
			mostSpecific = ClassUtils.isAssignable(candidate.getDeclaringClass(),
					mostSpecific.getDeclaringClass()) ? candidate : mostSpecific;
		}

		return mostSpecific;
	}

	private static List<String> generateUnbindingMethodNames(
			BindingNode.Effective<?, ?, ?> node, Class<?> resultClass) {
		List<String> names;

		if (node.getUnbindingMethodName() != null)
			names = Arrays.asList(node.getUnbindingMethodName());
		else
			names = generateUnbindingMethodNames(node.getName().getName(), false,
					resultClass);

		return names;
	}

	private static List<String> generateUnbindingMethodNames(String propertyName,
			boolean isIterable, Class<?> resultClass) {
		List<String> names = new ArrayList<>();

		names.add(propertyName);
		names.add(propertyName + "Value");
		if (isIterable) {
			for (String name : new ArrayList<>(names)) {
				names.add(name + "s");
				names.add(name + "List");
				names.add(name + "Set");
				names.add(name + "Collection");
				names.add(name + "Array");
			}
		}
		if (resultClass != null
				&& (resultClass.equals(Boolean.class) || resultClass
						.equals(boolean.class)))
			names.add("is" + capitalize(propertyName));

		List<String> namesAndBlank = new ArrayList<>(names);
		namesAndBlank.add("");

		for (String name : namesAndBlank) {
			names.add("get" + capitalize(name));
			names.add("to" + capitalize(name));
			names.add("compose" + capitalize(name));
			names.add("create" + capitalize(name));
		}

		return names;
	}

	private static String capitalize(String string) {
		return string == "" ? "" : Character.toUpperCase(string.charAt(0))
				+ string.substring(1);
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
