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
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.impl.schema.utilities.Methods;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideBuilder;
import uk.co.strangeskies.modabi.impl.schema.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.ChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.InputNode;
import uk.co.strangeskies.modabi.schema.InputNode.InputMemberType;
import uk.co.strangeskies.modabi.schema.InputNodeConfigurator;
import uk.co.strangeskies.reflection.ExecutableMember;
import uk.co.strangeskies.reflection.IntersectionType;
import uk.co.strangeskies.reflection.TypeMember;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypeVariableCapture;

public class InputNodeComponent<C extends InputNodeConfigurator<C, N>, N extends InputNode<N>> {
	private final ExecutableMember<?, ?> inputMember;
	private final InputMemberType inputMemberType;
	private final Boolean inMethodChained;
	private final Boolean allowInMethodResultCast;
	private final Boolean inMethodUnchecked;

	private final TypeToken<?> preInputType;
	private final TypeToken<?> postInputType;

	public InputNodeComponent(InputNodeConfiguratorImpl<C, N> configurator, TypeToken<?>... inMethodParameters) {
		this(configurator, Arrays.asList(inMethodParameters));
	}

	public InputNodeComponent(InputNodeConfiguratorImpl<C, N> configurator, List<TypeToken<?>> inMethodParameters) {

		inMethodChained = getOverride(configurator, InputNode::chainedInput, InputNodeConfigurator::getChainedInput)
				.orDefault(ifResolved(configurator,
						configurator.getContext().isConstructorExpected() || configurator.getContext().isStaticMethodExpected()))
				.get();

		inMethodUnchecked = getOverride(configurator, InputNode::uncheckedInput, InputNodeConfigurator::getUncheckedInput)
				.orDefault(ifResolved(configurator, false)).get();

		allowInMethodResultCast = inMethodChained != null && !inMethodChained ? null
				: getOverride(configurator, InputNode::castInput, InputNodeConfigurator::getCastInput)
						.orDefault(ifResolved(configurator, false)).get();

		ExecutableMember<?, ?> inputMember;
		InputMemberType inputMemberType = determineInputMemberType(configurator);
		if (inputMemberType == null) {
			try {
				inputMemberType = modifyInputMemberType(configurator.getContext(), InputMemberType.METHOD);
				inputMember = determineInputMethod(configurator, inputMemberType, inMethodParameters);
			} catch (Exception methodException) {
				try {
					inputMemberType = InputMemberType.FIELD;
					inputMember = determineInputField(configurator, inMethodParameters);
				} catch (Exception fieldException) {
					throw methodException;
				}
			}
		} else {
			switch (inputMemberType) {
			case FIELD:
				inputMember = determineInputField(configurator, inMethodParameters);
				break;
			case METHOD:
			case CONSTRUCTOR:
			case STATIC_METHOD:
				inputMember = determineInputMethod(configurator, inputMemberType, inMethodParameters);
				break;
			case NONE:
				inputMember = null;
				break;
			default:
				throw new UnsupportedOperationException();
			}
		}
		this.inputMember = inputMember;
		this.inputMemberType = inputMemberType;

		preInputType = determinePreInputType();
		postInputType = determinePostInputType(configurator);
	}

	protected <T> OverrideBuilder<T, C, ?, N> getOverride(InputNodeConfiguratorImpl<C, N> configurator,
			Function<N, T> valueFunction, Function<C, T> givenValueFunction) {
		return new OverrideBuilder<>(configurator, InputNodeConfiguratorImpl::getOverriddenNodes, valueFunction,
				givenValueFunction);
	}

	protected <T> OverrideBuilder<T, C, ?, N> getOverride(InputNodeConfiguratorImpl<C, N> configurator,
			Function<C, T> givenValueFunction) {
		return new OverrideBuilder<>(configurator, InputNodeConfiguratorImpl::getOverriddenNodes, n -> null,
				givenValueFunction);
	}

	private InputMemberType determineInputMemberType(InputNodeConfiguratorImpl<C, N> configurator) {
		return getOverride(configurator, InputNode::inputMemberType,
				c -> modifyInputMemberType(configurator.getContext(), c.getInputMemberType())).tryGet();
	}

	private InputMemberType modifyInputMemberType(SchemaNodeConfigurationContext context, InputMemberType type) {
		if (type == InputMemberType.METHOD) {
			if (context.isConstructorExpected()) {
				type = InputMemberType.CONSTRUCTOR;
			} else if (context.isStaticMethodExpected()) {
				type = InputMemberType.STATIC_METHOD;
			}
		}

		return type;
	}

	private boolean isResolved(InputNodeConfiguratorImpl<?, ?> configurator) {
		return configurator.getResult().concrete();
	}

	private <T> T ifResolved(InputNodeConfiguratorImpl<?, ?> configurator, T t) {
		return isResolved(configurator) ? t : null;
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

	public TypeMember<?> getInputMember() {
		return inputMember;
	}

	public TypeToken<?> getPreInputType() {
		return preInputType;
	}

	public TypeToken<?> getPostInputType() {
		return postInputType;
	}

	private ExecutableMember<?, ?> determineInputField(InputNodeConfiguratorImpl<C, N> configurator,
			List<TypeToken<?>> parameters) {
		throw new UnsupportedOperationException();
	}

	private ExecutableMember<?, ?> determineInputMethod(InputNodeConfiguratorImpl<C, N> configurator,
			InputMemberType inputMemberType, List<TypeToken<?>> parameters) {
		ExecutableMember<?, ?> inExecutableMember;

		if (!isResolved(configurator)) {
			inExecutableMember = null;
		} else {
			TypeToken<?> inputTargetType = configurator.getContext().inputTargetType();

			TypeToken<?> result = getResultType(configurator, configurator.getContext());

			/*
			 * cast parameter types to their raw types if unchecked
			 */
			if (inMethodUnchecked)
				parameters = parameters.stream().<TypeToken<?>> map(t -> TypeToken.over(t.getRawType()))
						.collect(Collectors.toList());

			/*
			 * first try to find and validate an inherited in method ...
			 */
			inExecutableMember = resolveOverriddenInMethod(configurator, inputTargetType, parameters);
			/*
			 * ... then if none exists, resolve one from scratch
			 */
			if (inExecutableMember == null) {
				if (inputMemberType == InputMemberType.CONSTRUCTOR) {
					inExecutableMember = Methods.findConstructor(inputTargetType, parameters).withTargetType(result);

				} else {
					String givenInMethodName = getOverride(configurator,
							n -> n.inputExecutable() == null ? null : n.inputExecutable().getName(),
							InputNodeConfigurator::getInputMember).tryGet();

					inExecutableMember = Methods.findMethod(
							generateInMethodNames(configurator.getResult().name(), givenInMethodName), inputTargetType,
							configurator.getContext().isStaticMethodExpected(), result, inMethodChained && allowInMethodResultCast,
							parameters);
				}
			}

			/*
			 * We're incorporating the preliminary types rather than those improved by
			 * accounting for reified provided values!
			 */

			configurator.getContext().boundSet().incorporate(inExecutableMember.getResolver().getBounds());
		}

		return inExecutableMember;
	}

	/*
	 * resolve the inherited in method or constructor, and make sure it is
	 * properly overridden if necessary
	 * 
	 * TODO handle some sort of constructor 'pseudo-override' behavior
	 */
	private ExecutableMember<?, ?> resolveOverriddenInMethod(InputNodeConfiguratorImpl<C, N> configurator,
			TypeToken<?> inputTargetType, List<TypeToken<?>> parameters) {
		Executable inExecutable = getOverride(configurator,
				n -> n.inputExecutable() == null ? null : n.inputExecutable().getMember(), c -> null).tryGet();

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

	private TypeToken<?> getResultType(InputNodeConfiguratorImpl<C, N> configurator,
			SchemaNodeConfigurationContext context) {
		if (inMethodChained) {
			TypeToken<?> resultType = getOverride(configurator, InputNode::postInputType,
					ChildNodeConfigurator::getPostInputType).validateOverride(TypeToken::isAssignableTo).orMerged((a, b) -> {
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
				throw new ModabiException(t -> t.inMethodMustBeChained(configurator.getResult().name(), inputMemberType));
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

	private TypeToken<?> determinePreInputType() {
		if (inputMemberType == InputMemberType.NONE) {
			return null;

		} else if (inputMember == null) {
			return null;

		} else {
			return inputMember.getDeclaringType();
		}
	}

	private TypeToken<?> determinePostInputType(InputNodeConfiguratorImpl<C, N> configurator) {
		TypeToken<?> postInputClass;

		if (inputMemberType == InputMemberType.NONE || (inMethodChained != null && !inMethodChained)) {
			postInputClass = configurator.getContext().inputTargetType();
		} else if (!isResolved(configurator) || inMethodChained == null) {
			postInputClass = getOverride(configurator, n -> n.postInputType() == null ? null : n.postInputType(),
					InputNodeConfigurator::getPostInputType).validateOverride(TypeToken::isAssignableTo).tryGet();
		} else {
			TypeToken<?> methodReturn;

			methodReturn = inputMember.getReturnType();

			if (methodReturn.getType() instanceof TypeVariableCapture)
				methodReturn = TypeToken
						.over(IntersectionType.from(Arrays.asList(((TypeVariableCapture) methodReturn.getType()).getUpperBounds()),
								methodReturn.getResolver().getBounds()))
						.withBoundsFrom(methodReturn.getResolver());

			TypeToken<?> localPostInputType = configurator.getThis().getPostInputType();

			if (localPostInputType == null || localPostInputType.isAssignableFrom(methodReturn))
				localPostInputType = methodReturn;

			TypeToken<?> finalPostInputType = localPostInputType;

			postInputClass = getOverride(configurator, n -> n.postInputType(),
					c -> c == configurator ? finalPostInputType : null).validateOverride(TypeToken::isAssignableTo).get();
		}

		return postInputClass;
	}
}
