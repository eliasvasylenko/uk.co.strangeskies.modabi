/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.modabi.impl.schema;

import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.impl.schema.utilities.Methods;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.impl.schema.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.ChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.InputNode;
import uk.co.strangeskies.reflection.IntersectionType;
import uk.co.strangeskies.reflection.Invokable;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypeVariableCapture;

public class InputNodeConfigurationHelper<N extends InputNode<N, E>, E extends InputNode.Effective<N, E>> {
	private final QualifiedName name;
	private final boolean isAbstract;

	private final String inMethodName;
	private final Invokable<?, ?> inMethod;
	private final Boolean inMethodChained;
	private final Boolean allowInMethodResultCast;
	private final Boolean inMethodUnchecked;

	private final TypeToken<?> preInputType;
	private final TypeToken<?> postInputType;

	private final OverrideMerge<N, ? extends ChildNodeConfigurator<?, N>> overrideMerge;
	private final SchemaNodeConfigurationContext<? super N> context;

	public InputNodeConfigurationHelper(boolean isAbstract, QualifiedName name,
			OverrideMerge<N, ? extends ChildNodeConfigurator<?, N>> overrideMerge,
			SchemaNodeConfigurationContext<? super N> context, List<TypeToken<?>> inMethodParameters) {
		this.isAbstract = isAbstract;
		this.name = name;
		this.overrideMerge = overrideMerge;
		this.context = context;

		inMethodChained = overrideMerge.getOverride(InputNode::isInMethodChained).orDefault(false).get();
		inMethodUnchecked = overrideMerge.getOverride(InputNode::isInMethodUnchecked).orDefault(false).get();
		allowInMethodResultCast = inMethodChained != null && !inMethodChained ? null
				: overrideMerge.getOverride(InputNode::isInMethodCast).orDefault(false).get();

		inMethod = inMethod(inMethodParameters);
		inMethodName = inMethodName();
		preInputType = preInputType();
		postInputType = postInputType();
	}

	public Boolean isInMethodChained() {
		return inMethodChained;
	}

	public Boolean isInMethodCast() {
		return allowInMethodResultCast;
	}

	public Boolean isInMethodUnchecked() {
		return inMethodUnchecked;
	}

	public Invokable<?, ?> getInMethod() {
		return inMethod;
	}

	public TypeToken<?> getPreInputType() {
		return preInputType;
	}

	public TypeToken<?> getPostInputType() {
		return postInputType;
	}

	public String getInMethodName() {
		return inMethodName;
	}

	private TypeToken<?> inputTargetType() {
		return context.inputTargetType();
	}

	private Invokable<?, ?> inMethod(List<TypeToken<?>> parameters) {
		Invokable<?, ?> inInvokable;

		String overriddenInMethodName = overrideMerge.getOverride(InputNode::getInMethodName).tryGet();

		if (!context.isInputExpected())
			if (overriddenInMethodName == null)
				overriddenInMethodName = "null";
			else if (!"null".equals(overriddenInMethodName))
				throw new SchemaException("In method name should not be provided for this node.");

		TypeToken<?> inputTargetType = inputTargetType();

		if (isAbstract || "null".equals(overriddenInMethodName)) {
			inInvokable = null;
		} else {
			try {
				TypeToken<?> result;
				if (inMethodChained) {
					TypeToken<?> resultType = overrideMerge.getOverride(InputNode::getPostInputType)
							.validate(TypeToken::isAssignableTo).tryGet();

					result = resultType == null ? null : resultType;
					if (result == null) {
						result = TypeToken.over(Object.class);
					}
				} else {
					result = null;
				}

				if (inMethodUnchecked)
					parameters = parameters.stream().<TypeToken<?>>map(t -> TypeToken.over(t.getRawType()))
							.collect(Collectors.toList());

				Executable inMethod = overrideMerge.getOverride(n -> n.effective() == null ? null : n.effective().getInMethod())
						.tryGet();
				if (inMethod != null) {
					inInvokable = Invokable.over(inMethod, inputTargetType);
					try {
						inInvokable = inInvokable.withLooseApplicability(parameters);
					} catch (Exception e) {
						if (inInvokable.isVariableArity()) {
							try {
								inInvokable = inInvokable.withVariableArityApplicability(parameters);
							} catch (Exception e2) {
								throw e;
							}
						} else {
							throw e;
						}
					}
				} else if (context.isConstructorExpected()) {
					inInvokable = Methods.findConstructor(inputTargetType, parameters).withTargetType(result);
				} else {
					inInvokable = Methods.findMethod(generateInMethodNames(name, overriddenInMethodName), inputTargetType,
							context.isStaticMethodExpected(), result, inMethodChained && allowInMethodResultCast, parameters);
				}

				context.boundSet().incorporate(inInvokable.getResolver().getBounds());
			} catch (Exception e) {
				throw new SchemaException("Cannot find input method for node '" + name + "' on class '" + inputTargetType
						+ "' with parameters '" + parameters + "'", e);
			}
		}

		return inInvokable;
	}

	private static List<String> generateInMethodNames(QualifiedName nodeName, String inheritedInMethodName) {
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
		return string == "" ? "" : Character.toUpperCase(string.charAt(0)) + string.substring(1);
	}

	private String inMethodName() {
		String inMethodName = overrideMerge.getOverride(InputNode::getInMethodName).tryGet();

		if (!context.isInputExpected() && inMethodName == null)
			inMethodName = "null";

		if (context.isInputExpected() && inMethodName == null && !isAbstract)
			inMethodName = inMethod.getExecutable().getName();

		return inMethodName;
	}

	private TypeToken<?> preInputType() {
		return (isAbstract || "null".equals(inMethodName)) ? null
				: TypeToken.over(inMethod.getExecutable().getDeclaringClass());
	}

	private TypeToken<?> postInputType() {
		TypeToken<?> postInputClass;

		if ("null".equals(inMethodName) || (inMethodChained != null && !inMethodChained)) {
			postInputClass = inputTargetType();
		} else if (isAbstract || inMethodChained == null) {
			postInputClass = overrideMerge.getOverride(n -> n.getPostInputType() == null ? null : n.getPostInputType())
					.validate(TypeToken::isAssignableTo).tryGet();
		} else {
			TypeToken<?> methodReturn;

			methodReturn = inMethod.getReturnType();

			if (methodReturn.getType() instanceof TypeVariableCapture)
				methodReturn = TypeToken
						.over(IntersectionType.from(Arrays.asList(((TypeVariableCapture) methodReturn.getType()).getUpperBounds()),
								methodReturn.getResolver().getBounds()))
						.withBoundsFrom(methodReturn.getResolver());

			TypeToken<?> localPostInputClass = overrideMerge.node().getPostInputType();

			if (localPostInputClass == null || localPostInputClass.isAssignableFrom(methodReturn))
				localPostInputClass = methodReturn;

			postInputClass = overrideMerge.getOverride(n -> n.getPostInputType(), localPostInputClass)
					.validate(TypeToken::isAssignableTo).get();
		}

		return postInputClass;
	}
}
