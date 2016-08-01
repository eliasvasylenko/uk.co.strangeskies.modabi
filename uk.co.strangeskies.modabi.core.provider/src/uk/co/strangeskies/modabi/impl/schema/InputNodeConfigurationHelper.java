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

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.ModabiProperties.ExecutableType;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.impl.schema.utilities.Methods;
import uk.co.strangeskies.modabi.impl.schema.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.ChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.InputNode;
import uk.co.strangeskies.modabi.schema.InputNode.InputMemberType;
import uk.co.strangeskies.modabi.schema.InputNodeConfigurator;
import uk.co.strangeskies.reflection.ExecutableMember;
import uk.co.strangeskies.reflection.IntersectionType;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypeVariableCapture;

public class InputNodeConfigurationHelper<N extends InputNode<N>> {
	private final QualifiedName name;
	private final boolean concrete;

	private final ExecutableMember<?, ?> inMethod;
	private final InputMemberType inputMemberType;
	private final Boolean inMethodChained;
	private final Boolean allowInMethodResultCast;
	private final Boolean inMethodUnchecked;

	private final TypeToken<?> preInputType;
	private final TypeToken<?> postInputType;

	private final SchemaNodeConfiguratorImpl<? extends InputNodeConfigurator<?, ?>, N> configurator;
	private final SchemaNodeConfigurationContext context;

	public InputNodeConfigurationHelper(boolean concrete, QualifiedName name,
			SchemaNodeConfiguratorImpl<? extends InputNodeConfigurator<?, ?>, N> configurator,
			SchemaNodeConfigurationContext context, List<TypeToken<?>> inMethodParameters) {
		this.concrete = concrete;
		this.name = name;
		this.configurator = configurator;
		this.context = context;

		inputMemberType = configurator.getOverride(InputNode::inputMemberType, InputNodeConfigurator::getInputMemberType)
				.get();
		inMethodChained = configurator.getOverride(InputNode::chainedInput, InputNodeConfigurator::getChainedInput)
				.orDefault(ifResolved(context.isConstructorExpected() || context.isStaticMethodExpected())).get();
		inMethodUnchecked = configurator.getOverride(InputNode::uncheckedInput, InputNodeConfigurator::getUncheckedInput)
				.orDefault(ifResolved(false)).get();
		allowInMethodResultCast = inMethodChained != null && !inMethodChained ? null
				: configurator.getOverride(InputNode::castInput, InputNodeConfigurator::getCastInput)
						.orDefault(ifResolved(false)).get();

		inMethod = inputMethod(inMethodParameters);
		preInputType = preInputType();
		postInputType = postInputType();
	}

	private boolean isResolved() {
		return concrete;
	}

	private <T> T ifResolved(T t) {
		return isResolved() ? t : null;
	}

	public InputMemberType getInputMemberType() {
		return inputMemberType;
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

	public ExecutableMember<?, ?> getInputMember() {
		return inMethod;
	}

	public TypeToken<?> getPreInputType() {
		return preInputType;
	}

	public TypeToken<?> getPostInputType() {
		return postInputType;
	}

	private ExecutableMember<?, ?> inputMethod(List<TypeToken<?>> parameters) {
		ExecutableMember<?, ?> inExecutableMember;

		if (!isResolved() || inputMemberType != InputMemberType.METHOD) {
			inExecutableMember = null;
		} else {
			String givenInMethodName = configurator
					.getOverride(n -> n.inputExecutable() == null ? null : n.inputExecutable().getName(),
							InputNodeConfigurator::getInputMember)
					.tryGet();

			TypeToken<?> inputTargetType = context.inputTargetType();

			try {
				TypeToken<?> result = getResultType();

				/*
				 * cast parameter types to their raw types if unchecked
				 */
				if (inMethodUnchecked)
					parameters = parameters.stream().<TypeToken<?>> map(t -> TypeToken.over(t.getRawType()))
							.collect(Collectors.toList());

				/*
				 * first try to find and validate an inherited in method ...
				 */
				inExecutableMember = resolveOverriddenInMethod(inputTargetType, parameters);
				/*
				 * ... then if none exists, resolve one from scratch
				 */
				if (inExecutableMember == null) {
					inExecutableMember = resolveInMethod(givenInMethodName, inputTargetType, result, parameters);
				}

				/*
				 * We're incorporating the preliminary types rather than those improved
				 * by accounting for reified provided values!
				 */

				context.boundSet().incorporate(inExecutableMember.getResolver().getBounds());
			} catch (Exception e) {
				List<TypeToken<?>> parametersFinal = parameters;
				ExecutableType type = context.isStaticMethodExpected() ? ExecutableType.STATIC_METHOD
						: (context.isConstructorExpected() ? ExecutableType.CONSTRUCTOR : ExecutableType.METHOD);

				throw new ModabiException(t -> t.noMemberFound(inputTargetType, parametersFinal, type), e);
			}
		}

		return inExecutableMember;
	}

	/*
	 * resolve the exact in method overload
	 */
	private ExecutableMember<?, ?> resolveInMethod(String givenInMethodName, TypeToken<?> inputTargetType,
			TypeToken<?> result, List<TypeToken<?>> parameters) throws NoSuchMethodException {

		if (context.isConstructorExpected()) {
			if (givenInMethodName != null && !givenInMethodName.equals("this")) {
				throw new ModabiException(t -> t.inMethodMustBeThis());
			}
			return Methods.findConstructor(inputTargetType, parameters).withTargetType(result);

		} else {

			return Methods.findMethod(generateInMethodNames(name, givenInMethodName), inputTargetType,
					context.isStaticMethodExpected(), result, inMethodChained && allowInMethodResultCast, parameters);
		}
	}

	/*
	 * resolve the inherited in method or constructor, and make sure it is
	 * properly overridden if necessary
	 * 
	 * TODO handle some sort of constructor 'pseudo-override' behavior
	 */
	private ExecutableMember<?, ?> resolveOverriddenInMethod(TypeToken<?> inputTargetType,
			List<TypeToken<?>> parameters) {
		Executable inExecutable = configurator
				.getOverride(n -> n.inputExecutable() == null ? null : n.inputExecutable().getMember(), c -> null).tryGet();

		ExecutableMember<?, ?> inExecutableMember;

		if (inExecutable == null) {
			inExecutableMember = null;
		} else {
			inExecutableMember = ExecutableMember.over(inExecutable, inputTargetType);
			try {
				inExecutableMember = inExecutableMember.withLooseApplicability(parameters);
			} catch (Exception e) {
				if (inExecutableMember.isVariableArity()) {
					try {
						inExecutableMember = inExecutableMember.withVariableArityApplicability(parameters);
					} catch (Exception e2) {
						throw e;
					}
				} else {
					throw e;
				}
			}
		}

		return inExecutableMember;
	}

	private TypeToken<?> getResultType() {
		if (inMethodChained) {
			TypeToken<?> resultType = configurator
					.<TypeToken<?>> getOverride(InputNode::postInputType, ChildNodeConfigurator::getPostInputType)
					.validate(TypeToken::isAssignableTo).orMerged((a, b) -> {
						/*
						 * If only one of the values is proper give precedence to it,
						 * otherwise choose arbitrarily:
						 */
						TypeToken<?> first;
						TypeToken<?> second;
						if (a.isProper()) {
							first = a;
							second = b;
						} else {
							first = b;
							second = a;
						}

						try {
							return first.withLooseCompatibilityTo(second);
						} catch (Exception e) {}

						try {
							return second.withLooseCompatibilityTo(first);
						} catch (Exception e) {}

						return null;
					}).tryGet();

			return resultType != null ? resultType : TypeToken.over(Object.class);
		} else {
			if (context.isConstructorExpected() || context.isStaticMethodExpected()) {
				ExecutableType type = context.isStaticMethodExpected() ? ExecutableType.STATIC_METHOD
						: ExecutableType.CONSTRUCTOR;
				throw new ModabiException(t -> t.inMethodMustBeChained(name, type));
			}

			return null;
		}
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

	private TypeToken<?> preInputType() {
		if (inputMemberType == InputMemberType.NONE) {
			return null;

		} else if (inMethod == null) {
			return null;

		} else {
			return inMethod.getDeclaringType();
		}
	}

	private TypeToken<?> postInputType() {
		TypeToken<?> postInputClass;

		if (inputMemberType == InputMemberType.NONE || (inMethodChained != null && !inMethodChained)) {
			postInputClass = context.inputTargetType();
		} else if (!isResolved() || inMethodChained == null) {
			postInputClass = configurator.getOverride(n -> n.postInputType() == null ? null : n.postInputType(),
					InputNodeConfigurator::getPostInputType).validate(TypeToken::isAssignableTo).tryGet();
		} else {
			TypeToken<?> methodReturn;

			methodReturn = inMethod.getReturnType();

			if (methodReturn.getType() instanceof TypeVariableCapture)
				methodReturn = TypeToken
						.over(IntersectionType.from(Arrays.asList(((TypeVariableCapture) methodReturn.getType()).getUpperBounds()),
								methodReturn.getResolver().getBounds()))
						.withBoundsFrom(methodReturn.getResolver());

			TypeToken<?> localPostInputType = configurator.getThis().getPostInputType();

			if (localPostInputType == null || localPostInputType.isAssignableFrom(methodReturn))
				localPostInputType = methodReturn;

			TypeToken<?> finalPostInputType = localPostInputType;

			postInputClass = configurator
					.getOverride(n -> n.postInputType(), c -> c == configurator ? finalPostInputType : null)
					.validate(TypeToken::isAssignableTo).get();
		}

		return postInputClass;
	}
}
