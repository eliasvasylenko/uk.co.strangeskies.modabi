package uk.co.strangeskies.modabi.schema.node.building.configuration.impl;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.reflect.TypeUtils;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.InputNode;
import uk.co.strangeskies.modabi.schema.node.building.configuration.ChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.Methods;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.SchemaNodeConfigurationContext;

public class InputNodeConfigurationHelper<N extends InputNode<N, E>, E extends InputNode.Effective<N, E>> {
	private final E effective;
	private final OverrideMerge<N, ? extends ChildNodeConfigurator<?, N>> overrideMerge;
	private final SchemaNodeConfigurationContext<? super N> context;

	public InputNodeConfigurationHelper(E effective,
			OverrideMerge<N, ? extends ChildNodeConfigurator<?, N>> overrideMerge,
			SchemaNodeConfigurationContext<? super N> context) {
		this.effective = effective;
		this.overrideMerge = overrideMerge;
		this.context = context;
	}

	public Boolean isInMethodChained() {
		return overrideMerge.getValue(InputNode::isInMethodChained, false);
	}

	public Boolean isInMethodCast() {
		return effective.isInMethodChained() != null
				&& !effective.isInMethodChained() ? null : overrideMerge.getValue(
				InputNode::isInMethodCast, false);
	}

	private Class<?> inputTargetClass() {
		return context.inputTargetClass(effective.getName());
	}

	public Executable inMethod(List<Type> parameters) {
		String overriddenInMethodName = overrideMerge
				.tryGetValue(InputNode::getInMethodName);

		if (!context.isInputExpected())
			if (overriddenInMethodName == null)
				overriddenInMethodName = "null";
			else if (overriddenInMethodName != "null")
				throw new SchemaException(
						"In method name should not be provided for this node.");

		Executable inMethod;

		if (effective.isAbstract() || "null".equals(overriddenInMethodName)) {
			inMethod = null;
		} else {
			try {
				Class<?> result;
				if (effective.isInMethodChained()) {
					result = effective.source().getPostInputClass();
					if (result == null)
						result = Object.class;
				} else
					result = null;

				if (context.isConstructorExpected())
					inMethod = Methods.findConstructor(inputTargetClass(),
							rawParameters(parameters));
				else
					inMethod = Methods.findMethod(
							generateInMethodNames(effective, overriddenInMethodName),
							inputTargetClass(),
							context.isStaticMethodExpected(),
							result,
							effective != null && effective.isInMethodChained()
									&& effective.isInMethodCast(), rawParameters(parameters));
			} catch (NoSuchMethodException e) {
				throw new SchemaException("Cannot find input method for node '"
						+ effective + "' on class '" + inputTargetClass()
						+ "' with parameters '" + parameters + "'.", e);
			}
		}

		return inMethod;
	}

	private List<Class<?>> rawParameters(List<Type> parameters) {
		return parameters.stream().map(p -> TypeUtils.getRawType(p, null))
				.collect(Collectors.toList());
	}

	private static List<String> generateInMethodNames(
			InputNode.Effective<?, ?> node, String inheritedInMethodName) {
		List<String> names;

		if (inheritedInMethodName != null)
			names = Arrays.asList(inheritedInMethodName);
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

	public static String capitalize(String string) {
		return string == "" ? "" : Character.toUpperCase(string.charAt(0))
				+ string.substring(1);
	}

	public String inMethodName() {
		String inMethodName = overrideMerge.tryGetValue(InputNode::getInMethodName);

		if (!context.isInputExpected() && inMethodName == null)
			inMethodName = "null";

		if (context.isInputExpected() && inMethodName == null
				&& !effective.isAbstract())
			inMethodName = effective.getInMethod().getName();

		return inMethodName;
	}

	public Class<?> preInputClass() {
		return (effective.isAbstract() || "null"
				.equals(effective.getInMethodName())) ? null : effective.getInMethod()
				.getDeclaringClass();
	}

	public Class<?> postInputClass() {
		Class<?> postInputClass;

		if ("null".equals(effective.getInMethodName())
				|| (effective.isInMethodChained() != null && !effective
						.isInMethodChained())) {
			postInputClass = inputTargetClass();
		} else if (effective.isAbstract()) {
			postInputClass = overrideMerge.tryGetValue(InputNode::getPostInputClass,
					(n, o) -> o.isAssignableFrom(n));
		} else {
			Class<?> methodReturn;

			if (context.isConstructorExpected())
				methodReturn = effective.getInMethod().getDeclaringClass();
			else
				methodReturn = ((Method) effective.getInMethod()).getReturnType();

			Class<?> localPostInputClass = overrideMerge.node().getPostInputClass();

			if (localPostInputClass == null
					|| localPostInputClass.isAssignableFrom(methodReturn))
				localPostInputClass = methodReturn;

			postInputClass = overrideMerge.getValueWithOverride(localPostInputClass,
					InputNode::getPostInputClass, (n, o) -> o.isAssignableFrom(n));
		}

		return postInputClass;
	}
}
