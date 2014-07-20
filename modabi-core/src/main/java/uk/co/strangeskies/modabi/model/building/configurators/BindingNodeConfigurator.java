package uk.co.strangeskies.modabi.model.building.configurators;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ClassUtils;

import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.BindingNode;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;

public interface BindingNodeConfigurator<S extends BindingNodeConfigurator<S, N, T>, N extends BindingNode<T>, T>
		extends SchemaNodeConfigurator<S, N> {
	public <V extends T> BindingNodeConfigurator<?, ?, V> dataClass(
			Class<V> dataClass);

	public S bindingStrategy(BindingStrategy strategy);

	public S unbindingStrategy(UnbindingStrategy strategy);

	public S bindingClass(Class<?> bindingClass);

	public S unbindingClass(Class<?> unbindingClass);

	public S unbindingMethod(String unbindingMethod);

	/*
	 * Method resolution
	 */

	public static Method findMethod(List<String> names, Class<?> receiver,
			Class<?> result, Class<?>... parameter) throws NoSuchMethodException {
		for (String methodName : names) {
			try {
				Method method = receiver.getMethod(methodName, parameter);
				if (method != null
						&& (result == null || ClassUtils.isAssignable(
								method.getReturnType(), result, true)))
					return method;
			} catch (NoSuchMethodException | SecurityException e) {
			}
		}
		throw new NoSuchMethodException("For "
				+ names
				+ " in "
				+ receiver
				+ " as [ "
				+ Arrays.asList(parameter).stream()
						.map(p -> p == null ? "WAT" : p.getName())
						.collect(Collectors.joining(", ")) + " ] -> " + result);
	}

	public static List<String> generateInMethodNames(BindingChildNode<?> node) {
		if (node.getInMethodName() != null)
			return Arrays.asList(node.getInMethodName());
		else
			return generateInMethodNames(node.getId());

	}

	public static List<String> generateInMethodNames(String propertyName) {
		List<String> names = new ArrayList<>();

		names.add(propertyName);
		names.add(propertyName + "Value");

		List<String> namesAndBlank = new ArrayList<>(names);
		namesAndBlank.add("");

		for (String name : namesAndBlank) {
			names.add("set" + capitalize(name));
			names.add("from" + capitalize(name));
			names.add("parse" + capitalize(name));
			names.add("add" + capitalize(name));
			names.add("put" + capitalize(name));
		}

		return names;
	}

	public static List<String> generateOutMethodNames(BindingChildNode<?> node) {
		return generateOutMethodNames(node, node.getDataClass());
	}

	public static List<String> generateOutMethodNames(BindingChildNode<?> node,
			Class<?> resultClass) {

		List<String> names = new ArrayList<>();

		if (node.getOutMethodName() != null)
			names.add(node.getOutMethodName());
		else
			names.addAll(generateOutMethodNames(node.getId(),
					node.isOutMethodIterable() != null && node.isOutMethodIterable(),
					resultClass));

		return names;
	}

	public static List<String> generateOutMethodNames(String propertyName,
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

	static String capitalize(String string) {
		return string == "" ? "" : string.substring(0, 1).toUpperCase()
				+ string.substring(1);
	}
}