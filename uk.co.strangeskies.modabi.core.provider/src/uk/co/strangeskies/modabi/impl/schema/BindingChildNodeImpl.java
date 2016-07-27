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
import uk.co.strangeskies.modabi.Abstractness;
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
import uk.co.strangeskies.reflection.InferenceVariable;
import uk.co.strangeskies.reflection.Invokable;
import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;

abstract class BindingChildNodeImpl<T, S extends BindingChildNode<T, S>> extends BindingNodeImpl<T, S>
		implements BindingChildNode<T, S> {
	private final SchemaNode<?> parent;

	private final Boolean nullIfOmitted;
	private final Range<Integer> occurrences;
	private final Boolean ordered;

	private final Boolean iterable;
	private String outMethodName;
	private Invokable<?, ?> outMethod;
	private final Boolean outMethodUnchecked;
	private final Boolean outMethodCast;

	private Invokable<?, ?> inMethod;
	private Boolean inMethodChained;
	private Boolean allowInMethodResultCast;
	private Boolean inMethodUnchecked;

	private TypeToken<?> preInputType;
	private TypeToken<?> postInputType;

	private final Boolean extensible;

	private final Boolean synchronous;

	protected BindingChildNodeImpl(BindingChildNodeConfiguratorImpl<?, S, T> configurator) {
		this(configurator, true);
	}

	protected BindingChildNodeImpl(BindingChildNodeConfiguratorImpl<?, S, T> configurator, boolean integrateIO) {
		super(configurator);

		parent = configurator.getResult();
		configurator.getContext().addChild(this);

		synchronous = configurator.getOverride(BindingChildNode::synchronous, BindingChildNodeConfigurator::getSynchronous)
				.orDefault(false).get();

		extensible = configurator.getExtensible() == null ? false : configurator.getExtensible();

		if (abstractness().isMoreThan(Abstractness.UNINFERRED) && !configurator.getContext().isAbstract() && !extensible())
			throw new ModabiException(t -> t.cannotBeAbstract(this));

		ordered = configurator.getOverride(BindingChildNode::ordered, BindingChildNodeConfigurator::getOrdered)
				.orDefault(true).get();

		occurrences = configurator.getOverride(BindingChildNode::occurrences, BindingChildNodeConfigurator::getOccurrences)
				.validate((v, o) -> o.contains(v)).orDefault(Range.between(1, 1)).get();

		iterable = configurator.getOverride(BindingChildNode::outMethodIterable, c -> {
			if (c.getOutMethodIterable() != null) {
				return c.getOutMethodIterable();
			}

			if (c.getOccurrences() != null && !c.getOccurrences().isValueAbove(2)) {
				return true;
			}

			return null;
		}).orDefault(false).get();

		outMethodUnchecked = configurator
				.getOverride(BindingChildNode::outMethodUnchecked, BindingChildNodeConfigurator::getOutMethodUnchecked)
				.orDefault(false).get();

		outMethodCast = configurator
				.getOverride(BindingChildNode::outMethodCast, BindingChildNodeConfigurator::getOutMethodCast).orDefault(false)
				.get();

		outMethodName = configurator.getOverride(n -> n.outMethod().getName(), BindingChildNodeConfigurator::getOutMethod)
				.tryGet();

		nullIfOmitted = configurator
				.getOverride(BindingChildNode::nullIfOmitted, BindingChildNodeConfigurator::getNullIfOmitted)
				.validate((n, o) -> o || !n).orDefault(false).get();

		if (integrateIO) {
			integrateIO(configurator);
		}
	}

	public void integrateIO(BindingChildNodeConfiguratorImpl<?, S, T> configurator) {
		Method overriddenOutMethod = (Method) configurator
				.getOverride(n -> n.outMethod() == null ? null : n.outMethod().getExecutable(), c -> null).tryGet();

		outMethod = hasOutMethod(configurator) ? getOutMethod(this, overriddenOutMethod,
				configurator.getContext().outputSourceType(), configurator.getContext().boundSet()) : null;

		if (outMethodName == null && hasOutMethod(configurator))
			outMethodName = outMethod.getName();

		InputNodeConfigurationHelper<S> inputNodeHelper = new InputNodeConfigurationHelper<>(abstractness(), name(),
				configurator, configurator.getContext(), Arrays.asList(dataType()));

		inMethodChained = inputNodeHelper.isInMethodChained();
		allowInMethodResultCast = inputNodeHelper.isInMethodCast();
		inMethodUnchecked = inputNodeHelper.isInMethodUnchecked();
		inMethod = inputNodeHelper.getInMethod();
		preInputType = inputNodeHelper.getPreInputType();
		postInputType = inputNodeHelper.getPostInputType();
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

	private boolean hasOutMethod(BindingChildNodeConfiguratorImpl<?, S, T> configurator) {
		return !"void".equals(outMethodName) && !(configurator.getContext().isAbstract()
				&& abstractness().isMoreThan(Abstractness.RESOLVED) && (outMethodName == null || "this".equals(outMethodName)));
	}

	@Override
	public TypeToken<?> preInputType() {
		return preInputType;
	}

	@Override
	public TypeToken<?> postInputType() {
		return postInputType;
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
	public final Invokable<?, ?> outMethod() {
		return outMethod;
	}

	@Override
	public final Boolean outMethodIterable() {
		return iterable;
	}

	@Override
	public Boolean outMethodUnchecked() {
		return outMethodUnchecked;
	}

	@Override
	public Boolean outMethodCast() {
		return outMethodCast;
	}

	@Override
	public final Invokable<?, ?> inMethod() {
		return inMethod;
	}

	@Override
	public final Boolean inMethodChained() {
		return inMethodChained;
	}

	@Override
	public Boolean inMethodCast() {
		return allowInMethodResultCast;
	}

	@Override
	public Boolean inMethodUnchecked() {
		return inMethodUnchecked;
	}

	@SuppressWarnings("unchecked")
	private static <U> TypeToken<Iterable<? extends U>> getIteratorType(BindingChildNode<U, ?> node) {
		boolean outMethodCast = node.outMethodCast() != null && node.outMethodCast();

		TypeToken<U> type = outMethodCast ? (TypeToken<U>) TypeToken.over(new InferenceVariable()) : node.dataType();
		if (type == null) {
			type = (TypeToken<U>) new TypeToken<Object>() {};
		}

		/*
		 * TODO properly put inference variable into bounds...
		 */

		TypeToken<Iterable<? extends U>> iterableType = new TypeToken<Iterable<? extends U>>() {}
				.withTypeArgument(new TypeParameter<U>() {}, type.wrapPrimitive());

		return iterableType;

	}

	protected static Invokable<?, ?> getOutMethod(BindingChildNode<?, ?> node, Method inheritedOutMethod,
			TypeToken<?> receiverType, BoundSet bounds) {
		if (receiverType == null) {
			throw new ModabiException(t -> t.cannotFindOutMethodWithoutTargetType(node));
		}

		boolean outMethodCast = node.outMethodCast() != null && node.outMethodCast();

		TypeToken<?> resultType = ((node.outMethodIterable() != null && node.outMethodIterable()) ? getIteratorType(node)
				: node.dataType());

		if (node.outMethodUnchecked() != null && node.outMethodUnchecked())
			resultType = TypeToken.over(resultType.getRawType());

		if (resultType == null)
			throw new ModabiException(t -> t.cannotFindOutMethodWithoutResultType(node));

		Invokable<?, ?> outMethod;
		if ("this".equals(node.configurator().getOutMethod())) {
			if (!resultType.isAssignableFrom(receiverType.resolve())) {
				TypeToken<?> resultTypeFinal = resultType;
				throw new ModabiException(t -> t.incompatibleTypes(receiverType.getType(), resultTypeFinal.getType()));
			}

			outMethod = null;

			resultType.incorporateInto(bounds);
			ConstraintFormula.reduce(Kind.LOOSE_COMPATIBILILTY, receiverType.getType(), resultType.getType(), bounds);
		} else if (inheritedOutMethod != null) {
			try {
				outMethod = Invokable.over(inheritedOutMethod, receiverType).withLooseApplicability();
			} catch (Exception e) {
				outMethod = Invokable.over(inheritedOutMethod, receiverType).withVariableArityApplicability();
			}

			if (outMethodCast) {
				// TODO enforce castability
			} else {
				outMethod = outMethod.withTargetType(resultType);
			}
		} else {
			outMethod = Methods.findMethod(generateOutMethodNames(node, resultType.getRawType()), receiverType, false,
					resultType, outMethodCast);
		}

		if (outMethod != null) {
			bounds.incorporate(outMethod.getResolver().getBounds());
		}

		return outMethod;
	}

	private static List<String> generateOutMethodNames(BindingChildNode<?, ?> node, Class<?> resultClass) {
		List<String> names;

		if (node.configurator().getOutMethod() != null)
			names = Arrays.asList(node.configurator().getOutMethod());
		else
			names = generateUnbindingMethodNames(node.name().getName(),
					node.outMethodIterable() != null && node.outMethodIterable(), resultClass);

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
