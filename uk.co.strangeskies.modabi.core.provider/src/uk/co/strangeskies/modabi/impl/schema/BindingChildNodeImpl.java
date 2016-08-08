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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.impl.schema.utilities.Methods;
import uk.co.strangeskies.modabi.schema.BindingChildNode;
import uk.co.strangeskies.modabi.schema.BindingChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.ConstraintFormula;
import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.reflection.ExecutableMember;
import uk.co.strangeskies.reflection.FieldMember;
import uk.co.strangeskies.reflection.InferenceVariable;
import uk.co.strangeskies.reflection.TypeMember;
import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;

abstract class BindingChildNodeImpl<T, S extends BindingChildNode<T, S>> extends BindingNodeImpl<T, S>
		implements BindingChildNode<T, S> {
	private final SchemaNode<?> parent;

	private final Boolean nullIfOmitted;
	private final Range<Integer> occurrences;
	private final Boolean ordered;

	private OutputMemberType outputMemberType;
	private TypeMember<?> outputMember;
	private final Boolean uncheckedOutput;
	private final Boolean castOutput;
	private final Boolean iterableOutput;

	private final Boolean extensible;

	private final Boolean synchronous;

	protected <C extends BindingChildNodeConfigurator<C, S, T>> BindingChildNodeImpl(
			BindingChildNodeConfiguratorImpl<C, S, T> configurator) {
		this(configurator, true);
	}

	protected <C extends BindingChildNodeConfigurator<C, S, T>> BindingChildNodeImpl(
			BindingChildNodeConfiguratorImpl<C, S, T> configurator, boolean integrateIO) {
		super(configurator);

		parent = configurator.getContext().parent();

		synchronous = configurator.getOverride(BindingChildNode::synchronous, BindingChildNodeConfigurator::getSynchronous)
				.orDefault(false).get();

		extensible = configurator.getExtensible() == null ? false : configurator.getExtensible();

		if (!concrete() && !configurator.getContext().isAbstract() && !extensible())
			throw new ModabiException(t -> t.cannotBeAbstract(this));

		ordered = configurator.getOverride(BindingChildNode::ordered, BindingChildNodeConfigurator::getOrdered)
				.orDefault(true).get();

		occurrences = configurator.getOverride(BindingChildNode::occurrences, BindingChildNodeConfigurator::getOccurrences)
				.validateOverride((v, o) -> o.contains(v)).orDefault(Range.between(1, 1)).get();

		iterableOutput = configurator.getOverride(BindingChildNode::iterableOutput, c -> {
			if (c.getIterableOutput() != null) {
				return c.getIterableOutput();
			}

			if (c.getOccurrences() != null && !c.getOccurrences().isValueAbove(2)) {
				return true;
			}

			return null;
		}).orDefault(false).get();

		uncheckedOutput = configurator
				.getOverride(BindingChildNode::uncheckedOutput, BindingChildNodeConfigurator::getUncheckedOutput)
				.orDefault(false).get();

		castOutput = configurator.getOverride(BindingChildNode::castOutput, BindingChildNodeConfigurator::getCastOutput)
				.orDefault(false).get();

		nullIfOmitted = configurator
				.getOverride(BindingChildNode::nullIfOmitted, BindingChildNodeConfigurator::getNullIfOmitted)
				.validateOverride((n, o) -> o || !n).orDefault(false).get();

		if (integrateIO) {
			integrateIO(configurator);
		}
	}

	protected abstract InputNodeComponent<?, ?> getInputNodeComponent();

	public <C extends BindingChildNodeConfigurator<C, S, T>> void integrateIO(
			BindingChildNodeConfiguratorImpl<C, S, T> configurator) {
		TypeMember<?> outputMember;
		OutputMemberType outputMemberType = configurator
				.getOverride(BindingChildNode::outputMemberType, BindingChildNodeConfigurator::getOutputMemberType).tryGet();

		if (outputMemberType == null) {
			try {
				outputMember = getOutMethod(configurator);
				outputMemberType = OutputMemberType.METHOD;
			} catch (Exception methodException) {
				try {
					outputMember = getOutField(configurator);
					outputMemberType = OutputMemberType.FIELD;
				} catch (Exception fieldException) {
					throw methodException;
				}
			}
		} else {
			switch (outputMemberType) {
			case FIELD:
				outputMember = getOutField(configurator);
			case METHOD:
				outputMember = getOutMethod(configurator);
				break;
			case NONE:
			case SELF:
				outputMember = null;
				break;
			default:
				throw new UnsupportedOperationException();
			}
		}
		this.outputMember = outputMember;
		this.outputMemberType = outputMemberType;
	}

	@Override
	public InputMemberType inputMemberType() {
		return getInputNodeComponent().getInputMemberType();
	}

	@Override
	public final ExecutableMember<?, ?> inputExecutable() {
		return inputMemberType() == InputMemberType.METHOD
				? (ExecutableMember<?, ?>) getInputNodeComponent().getInputMember() : null;
	}

	@Override
	public FieldMember<?, ?> inputField() {
		return inputMemberType() == InputMemberType.FIELD ? (FieldMember<?, ?>) getInputNodeComponent().getInputMember()
				: null;
	}

	@Override
	public final Boolean chainedInput() {
		return getInputNodeComponent().isInMethodChained();
	}

	@Override
	public Boolean castInput() {
		return getInputNodeComponent().isInMethodCast();
	}

	@Override
	public Boolean uncheckedInput() {
		return getInputNodeComponent().isInMethodUnchecked();
	}

	@Override
	public TypeToken<?> postInputType() {
		return getInputNodeComponent().getPostInputType();
	}

	@Override
	public TypeToken<?> preInputType() {
		return getInputNodeComponent().getPreInputType();
	}

	@Override
	public BindingChildNodeConfigurator<?, S, T> configurator() {
		return (BindingChildNodeConfigurator<?, S, T>) super.configurator();
	}

	@Override
	public Boolean synchronous() {
		return synchronous;
	}

	@Override
	public final Boolean nullIfOmitted() {
		return nullIfOmitted;
	}

	@Override
	public Schema schema() {
		return root().schema();
	}

	@Override
	public SchemaNode<?> parent() {
		return parent;
	}

	@Override
	public Boolean ordered() {
		return ordered;
	}

	@Override
	public final Boolean extensible() {
		return extensible;
	}

	@Override
	public final Range<Integer> occurrences() {
		return occurrences;
	}

	@Override
	public OutputMemberType outputMemberType() {
		return outputMemberType;
	}

	@Override
	public final ExecutableMember<?, ?> outputMethod() {
		return outputMemberType == OutputMemberType.METHOD ? (ExecutableMember<?, ?>) outputMember : null;
	}

	@Override
	public FieldMember<?, ?> outputField() {
		return outputMemberType == OutputMemberType.FIELD ? (FieldMember<?, ?>) outputMember : null;
	}

	@Override
	public final Boolean iterableOutput() {
		return iterableOutput;
	}

	@Override
	public Boolean uncheckedOutput() {
		return uncheckedOutput;
	}

	@Override
	public Boolean castOutput() {
		return castOutput;
	}

	@SuppressWarnings("unchecked")
	private TypeToken<Iterable<? extends T>> getIteratorType() {
		boolean outMethodCast = castOutput() != null && castOutput();

		TypeToken<T> type = outMethodCast ? (TypeToken<T>) TypeToken.over(new InferenceVariable()) : dataType();
		if (type == null) {
			type = (TypeToken<T>) new TypeToken<Object>() {};
		}

		/*
		 * TODO properly put inference variable into bounds...
		 */

		TypeToken<Iterable<? extends T>> iterableType = new TypeToken<Iterable<? extends T>>() {}
				.withTypeArgument(new TypeParameter<T>() {}, type.wrapPrimitive());

		return iterableType;

	}

	protected ExecutableMember<?, ?> getOutField(BindingChildNodeConfiguratorImpl<?, S, T> configurator) {
		throw new UnsupportedOperationException();
	}

	protected ExecutableMember<?, ?> getOutMethod(BindingChildNodeConfiguratorImpl<?, S, T> configurator) {
		TypeToken<?> receiverType = configurator.getContext().outputSourceType();
		BoundSet bounds = configurator.getContext().boundSet();

		if (receiverType == null) {
			throw new ModabiException(t -> t.cannotFindOutMethodWithoutTargetType(this));
		}

		Method overriddenOutMethod = (Method) configurator
				.getOverride(n -> n.outputMethod() == null ? null : n.outputMethod().getMember(), c -> null).tryGet();

		boolean outMethodCast = castOutput() != null && castOutput();

		TypeToken<?> resultType = ((iterableOutput() != null && iterableOutput()) ? getIteratorType() : dataType());

		if (uncheckedOutput() != null && uncheckedOutput())
			resultType = TypeToken.over(resultType.getRawType());

		if (resultType == null)
			throw new ModabiException(t -> t.cannotFindOutMethodWithoutResultType(this));

		ExecutableMember<?, ?> outMethod;
		if ("this".equals(configurator.getOutputMember())) {
			if (!resultType.isAssignableFrom(receiverType.resolve())) {
				TypeToken<?> resultTypeFinal = resultType;
				throw new ModabiException(t -> t.incompatibleTypes(receiverType.getType(), resultTypeFinal.getType()));
			}

			outMethod = null;

			resultType.incorporateInto(bounds);
			ConstraintFormula.reduce(Kind.LOOSE_COMPATIBILILTY, receiverType.getType(), resultType.getType(), bounds);
		} else if (overriddenOutMethod != null) {
			try {
				outMethod = ExecutableMember.over(overriddenOutMethod, receiverType).withLooseApplicability();
			} catch (Exception e) {
				outMethod = ExecutableMember.over(overriddenOutMethod, receiverType).withVariableArityApplicability();
			}

			if (outMethodCast) {
				// TODO enforce castability
			} else {
				outMethod = outMethod.withTargetType(resultType);
			}
		} else {
			outMethod = Methods.findMethod(generateOutMethodNames(this, resultType.getRawType()), receiverType, false,
					resultType, outMethodCast);
		}

		if (outMethod != null) {
			bounds.incorporate(outMethod.getResolver().getBounds());
		}

		return outMethod;
	}

	private static List<String> generateOutMethodNames(BindingChildNode<?, ?> node, Class<?> resultClass) {
		List<String> names;

		if (node.configurator().getOutputMember() != null)
			names = Arrays.asList(node.configurator().getOutputMember());
		else
			names = generateUnbindingMethodNames(node.name().getName(),
					node.iterableOutput() != null && node.iterableOutput(), resultClass);

		return names;
	}

	@Override
	public boolean equals(Object that) {
		return super.equals(that) && that instanceof ChildNode && Objects.equals(parent(), ((ChildNode<?>) that).parent());
	}

	@Override
	public final int hashCode() {
		return super.hashCode() ^ Objects.hashCode(parent());
	}
}
