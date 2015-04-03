/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.provider.
 *
 * uk.co.strangeskies.modabi.core.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.schema.node.building.configuration.impl;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.InputNode;
import uk.co.strangeskies.modabi.schema.node.building.configuration.ChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.Methods;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.reflection.TypeToken;

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

	private TypeToken<?> inputTargetClass() {
		return context.inputTargetType(effective.getName());
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
				TypeToken<?> result;
				if (effective.isInMethodChained()) {
					result = effective.source().getPostInputType() == null ? null
							: TypeToken.of(effective.source().getPostInputType());
					if (result == null)
						result = TypeToken.of(Object.class);
				} else
					result = null;

				if (context.isConstructorExpected())
					inMethod = Methods.findConstructor(inputTargetClass(),
							parameterTokens(parameters)).getExecutable();
				else
					inMethod = Methods.findMethod(
							generateInMethodNames(effective, overriddenInMethodName),
							inputTargetClass(),
							context.isStaticMethodExpected(),
							result,
							effective != null && effective.isInMethodChained()
									&& effective.isInMethodCast(), parameterTokens(parameters))
							.getExecutable();
			} catch (NoSuchMethodException e) {
				throw new SchemaException("Cannot find input method for node '"
						+ effective + "' on class '" + inputTargetClass()
						+ "' with parameters '" + parameters + "'.", e);
			}
		}

		return inMethod;
	}

	private List<TypeToken<?>> parameterTokens(List<Type> parameters) {
		return parameters.stream().map(TypeToken::of).collect(Collectors.toList());
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

	public TypeToken<?> preInputType() {
		return (effective.isAbstract() || "null"
				.equals(effective.getInMethodName())) ? null : TypeToken.of(effective
				.getInMethod().getDeclaringClass());
	}

	public TypeToken<?> postInputType() {
		TypeToken<?> postInputClass;

		if ("null".equals(effective.getInMethodName())
				|| (effective.isInMethodChained() != null && !effective
						.isInMethodChained())) {
			postInputClass = inputTargetClass();
		} else if (effective.isAbstract()) {
			postInputClass = overrideMerge.tryGetValue(
					n -> n.getPostInputType() == null ? null : TypeToken.of(n
							.getPostInputType()), (n, o) -> o.isAssignableFrom(n));
		} else {
			Class<?> methodReturn;

			if (context.isConstructorExpected())
				methodReturn = effective.getInMethod().getDeclaringClass();
			else
				methodReturn = ((Method) effective.getInMethod()).getReturnType();

			Type localPostInputClass = overrideMerge.node().getPostInputType();

			if (localPostInputClass == null
					|| TypeToken.of(localPostInputClass).isAssignableFrom(methodReturn))
				localPostInputClass = methodReturn;

			postInputClass = overrideMerge
					.getValueWithOverride(
							TypeToken.of(localPostInputClass),
							n -> n.getPostInputType() == null ? null : TypeToken.of(n
									.getPostInputType()), (n, o) -> o.isAssignableFrom(n));
		}

		return postInputClass;
	}
}
