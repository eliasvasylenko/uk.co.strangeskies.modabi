package uk.co.strangeskies.modabi.model.building.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ClassUtils;

import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.BindingNode;
import uk.co.strangeskies.modabi.model.nodes.BindingNode.Effective;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;

public class Methods {
	private Methods() {
	}

	public static Method getInMethod(BindingChildNode.Effective<?, ?, ?> node,
			Class<?> receiverClass, Method inheritedInMethod) {
		try {
			return (receiverClass == null || node.getDataClass() == null || node
					.getInMethodName() == null) ? null
					: findMethod(generateInMethodNames(node), receiverClass, null,
							node.getDataClass());
		} catch (NoSuchMethodException e) {
			throw new SchemaException(e);
		}
	}

	public static Method getOutMethod(BindingChildNode.Effective<?, ?, ?> node,
			Class<?> targetClass, Method inheritedOutMethod) {
		try {
			Class<?> resultClass = (node.isOutMethodIterable() != null && node
					.isOutMethodIterable()) ? Iterable.class : node.getDataClass();

			Method outMethod;
			if (node.getOutMethodName() != null
					&& node.getOutMethodName().equals("this")) {
				if (!resultClass.isAssignableFrom(targetClass))
					throw new SchemaException();
				outMethod = null;
			} else if (targetClass == null || resultClass == null)
				outMethod = null;
			else {
				outMethod = findMethod(generateOutMethodNames(node, resultClass),
						targetClass, resultClass);

				if (inheritedOutMethod != null && !outMethod.equals(inheritedOutMethod))
					throw new SchemaException();
			}

			return outMethod;
		} catch (NoSuchMethodException e) {
			throw new SchemaException(e);
		}
	}

	private static List<String> generateInMethodNames(
			BindingChildNode.Effective<?, ?, ?> node) {
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
		if (node.getDataClass() == null)
			return null;

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
			Effective<?, ?, ?> node, Function<Effective<?, ?, ?>, Class<?>> nodeClass) {
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
		try {
			return findMethod(generateUnbindingMethodNames(node, result), receiver,
					result, parameters.toArray(new Class<?>[] {}));
		} catch (NoSuchMethodException | SecurityException e) {
			throw new SchemaException(e);
		}
	}

	private static Method findMethod(List<String> names, Class<?> receiver,
			Class<?> result, Class<?>... parameters) throws NoSuchMethodException {
		return Stream
				.concat(Arrays.stream(receiver.getMethods()),
						Arrays.stream(Object.class.getMethods()))
				.filter(
						m -> {
							if (!names.contains(m.getName()))
								return false;

							Class<?>[] methodParameters = m.getParameterTypes();
							int i = 0;
							for (Class<?> parameter : parameters)
								if (!ClassUtils.isAssignable(parameter, methodParameters[i++]))
									return false;

							return result == null
									|| ClassUtils.isAssignable(m.getReturnType(), result, true);
						}).findAny().orElse(null);
		/*-
		.orElseThrow(
				() -> new NoSuchMethodException("For "
						+ names
						+ " in "
						+ receiver
						+ " as [ "
						+ Arrays.asList(parameters).stream()
								.map(p -> p == null ? "WAT" : p.getName())
								.collect(Collectors.joining(", ")) + " ] -> " + result));*/
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
			BindingNode.Effective<?, ?, ?> node, boolean isAbstract) {
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

										if (!isAbstract && !dataNode.isValueProvided())
											throw new SchemaException("Unbinding parameter node '"
													+ dataNode + "' for '" + p
													+ "' must provide a value.");

										return dataNode;
									}
								}).collect(Collectors.toList());
	}
}
