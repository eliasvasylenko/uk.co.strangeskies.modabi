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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.InputNode;
import uk.co.strangeskies.modabi.schema.node.building.configuration.ChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.Methods;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.reflection.Invokable;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.Types;

public class InputNodeConfigurationHelper<N extends InputNode<N, E>, E extends InputNode.Effective<N, E>> {
	private final QualifiedName name;
	private final boolean isAbstract;

	private String inMethodName;
	private final Invokable<?, ?> inMethod;
	private final Boolean inMethodChained;
	private final Boolean allowInMethodResultCast;

	private final Type preInputType;
	private final Type postInputType;

	private final OverrideMerge<N, ? extends ChildNodeConfigurator<?, N>> overrideMerge;
	private final SchemaNodeConfigurationContext<? super N> context;

	public InputNodeConfigurationHelper(boolean isAbstract, QualifiedName name,
			OverrideMerge<N, ? extends ChildNodeConfigurator<?, N>> overrideMerge,
			SchemaNodeConfigurationContext<? super N> context,
			List<TypeToken<?>> inMethodParameters) {
		this.isAbstract = isAbstract;
		this.name = name;
		this.overrideMerge = overrideMerge;
		this.context = context;

		inMethodChained = determineInMethodChained();
		allowInMethodResultCast = determineInMethodCast();
		inMethod = inMethod(inMethodParameters);
		inMethodName = inMethodName();
		preInputType = preInputType() == null ? null : preInputType().getType();
		postInputType = postInputType() == null ? null : postInputType().getType();
	}

	public Boolean isInMethodChained() {
		return inMethodChained;
	}

	public Boolean isInMethodCast() {
		return allowInMethodResultCast;
	}

	public Invokable<?, ?> getInMethod() {
		return inMethod;
	}

	public Type getPreInputType() {
		return preInputType;
	}

	public Type getPostInputType() {
		return postInputType;
	}

	public String getInMethodName() {
		return inMethodName;
	}

	private Boolean determineInMethodChained() {
		return overrideMerge.getValue(InputNode::isInMethodChained, false);
	}

	private Boolean determineInMethodCast() {
		return inMethodChained != null && !inMethodChained ? null : overrideMerge
				.getValue(InputNode::isInMethodCast, false);
	}

	private TypeToken<?> inputTargetClass() {
		return context.inputTargetType(name);
	}

	private Invokable<?, ?> inMethod(List<TypeToken<?>> parameters) {
		String overriddenInMethodName = overrideMerge
				.tryGetValue(InputNode::getInMethodName);

		if (!context.isInputExpected())
			if (overriddenInMethodName == null)
				overriddenInMethodName = "null";
			else if (overriddenInMethodName != "null")
				throw new SchemaException(
						"In method name should not be provided for this node.");

		TypeToken<?> inputTargetType = inputTargetClass();

		Invokable<?, ?> inMethod;
		if (isAbstract || "null".equals(overriddenInMethodName)) {
			inMethod = null;
		} else {
			try {
				TypeToken<?> result;
				if (inMethodChained) {
					Type resultType = overrideMerge.tryGetValue(
							InputNode::getPostInputType, Types::isAssignable);
					result = resultType == null ? null : TypeToken.of(resultType);
					if (result == null)
						result = TypeToken.of(Object.class);
				} else
					result = null;

				if (context.isConstructorExpected())
					inMethod = Methods.findConstructor(inputTargetType, parameters);
				else
					inMethod = Methods.findMethod(
							generateInMethodNames(name, overriddenInMethodName),
							inputTargetType, context.isStaticMethodExpected(), result,
							inMethodChained && allowInMethodResultCast, parameters);

				inMethod = inMethod.inferParameterTypes().infer();

				System.out.println(inMethod);

				context.boundSet().incorporate(inMethod.getResolver().getBounds());
			} catch (NoSuchMethodException e) {
				throw new SchemaException("Cannot find input method for node '" + name
						+ "' on class '" + inputTargetType + "' with parameters '"
						+ parameters + "'.", e);
			}
		}

		return inMethod;
	}

	private static List<String> generateInMethodNames(QualifiedName nodeName,
			String inheritedInMethodName) {
		List<String> names;

		if (inheritedInMethodName != null)
			names = Arrays.asList(inheritedInMethodName);
		else {
			names = new ArrayList<>();

			names.add(nodeName.getName());
			names.add(nodeName.getName() + "Value");

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

	static String capitalize(String string) {
		return string == "" ? "" : Character.toUpperCase(string.charAt(0))
				+ string.substring(1);
	}

	private String inMethodName() {
		String inMethodName = overrideMerge.tryGetValue(InputNode::getInMethodName);

		if (!context.isInputExpected() && inMethodName == null)
			inMethodName = "null";

		if (context.isInputExpected() && inMethodName == null && !isAbstract)
			inMethodName = inMethod.getExecutable().getName();

		return inMethodName;
	}

	private TypeToken<?> preInputType() {
		return (isAbstract || "null".equals(inMethodName)) ? null : TypeToken
				.of(inMethod.getExecutable().getDeclaringClass());
	}

	private TypeToken<?> postInputType() {
		TypeToken<?> postInputClass;

		if ("null".equals(inMethodName)
				|| (inMethodChained != null && !inMethodChained)) {
			postInputClass = inputTargetClass();
		} else if (isAbstract) {
			postInputClass = overrideMerge.tryGetValue(
					n -> n.getPostInputType() == null ? null : TypeToken.of(n
							.getPostInputType()), (n, o) -> o.isAssignableFrom(n));
		} else {
			TypeToken<?> methodReturn;

			if (context.isConstructorExpected())
				methodReturn = inMethod.getReceiverType();
			else
				methodReturn = inMethod.getReturnType();

			Type localPostInputClass = overrideMerge.node().getPostInputType();

			if (localPostInputClass == null
					|| TypeToken.of(localPostInputClass).isAssignableFrom(methodReturn))
				localPostInputClass = methodReturn.getType();

			postInputClass = overrideMerge
					.getValueWithOverride(
							TypeToken.of(localPostInputClass),
							n -> n.getPostInputType() == null ? null : TypeToken.of(n
									.getPostInputType()), (n, o) -> o.isAssignableFrom(n));
		}

		return postInputClass;
	}
}
