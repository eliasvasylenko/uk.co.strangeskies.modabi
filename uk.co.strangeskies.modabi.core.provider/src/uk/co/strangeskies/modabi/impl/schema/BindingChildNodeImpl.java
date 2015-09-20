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
package uk.co.strangeskies.modabi.impl.schema;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.impl.schema.utilities.Methods;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.BindingChildNode;
import uk.co.strangeskies.modabi.schema.InputNode;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.ConstraintFormula;
import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.reflection.InferenceVariable;
import uk.co.strangeskies.reflection.Invokable;
import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.PropertySet;

abstract class BindingChildNodeImpl<T, S extends BindingChildNode<T, S, E>, E extends BindingChildNode.Effective<T, S, E>>
		extends BindingNodeImpl<T, S, E> implements BindingChildNode<T, S, E> {
	protected static abstract class Effective<T, S extends BindingChildNode<T, S, E>, E extends BindingChildNode.Effective<T, S, E>>
			extends BindingNodeImpl.Effective<T, S, E> implements
			BindingChildNode.Effective<T, S, E> {
		private final Range<Integer> occurrences;

		private final Boolean iterable;
		private String outMethodName;
		private final Method outMethod;
		private final Boolean outMethodUnchecked;
		private final Boolean outMethodCast;

		private String inMethodName;
		private final Executable inMethod;
		private final Boolean inMethodChained;
		private final Boolean allowInMethodResultCast;
		private final Boolean inMethodUnchecked;

		private final Boolean extensible;
		private final Boolean ordered;

		private final TypeToken<?> preInputType;
		private final TypeToken<?> postInputType;

		protected Effective(
				OverrideMerge<S, ? extends BindingChildNodeConfiguratorImpl<?, S, ?>> overrideMerge) {
			super(overrideMerge);

			if (isAbstract()
					&& !overrideMerge.configurator().getContext().isAbstract())
				throw new SchemaException("Node '" + getName()
						+ "' has no abstract or extensible parents, so cannot be abstract.");

			extensible = overrideMerge
					.getValue(BindingChildNode::isExtensible, false);

			ordered = overrideMerge.getValue(BindingChildNode::isOrdered, true);

			occurrences = overrideMerge.getValue(BindingChildNode::occurrences,
					(v, o) -> o.contains(v), Range.create(1, 1));

			iterable = overrideMerge.getValue(BindingChildNode::isOutMethodIterable,
					false);

			outMethodUnchecked = overrideMerge.getValue(
					BindingChildNode::isOutMethodUnchecked, false);

			outMethodCast = overrideMerge.getValue(BindingChildNode::isOutMethodCast,
					false);

			outMethodName = overrideMerge
					.tryGetValue(BindingChildNode::getOutMethodName);

			Method overriddenOutMethod = overrideMerge
					.tryGetValue(n -> n.effective() == null ? null : n.effective()
							.getOutMethod());

			Invokable<?, ?> outInvokable = hasOutMethod(overrideMerge) ? getOutMethod(
					this, overriddenOutMethod, overrideMerge.configurator().getContext()
							.outputSourceType(), overrideMerge.configurator().getContext()
							.boundSet()) : null;

			outMethod = outInvokable == null ? null : (Method) outInvokable
					.getExecutable();

			if (outMethodName == null && hasOutMethod(overrideMerge))
				outMethodName = outMethod.getName();

			InputNodeConfigurationHelper<S, E> inputNodeHelper = new InputNodeConfigurationHelper<S, E>(
					isAbstract(), getName(), overrideMerge, overrideMerge.configurator()
							.getContext(), Arrays.asList(getDataType()));

			inMethodChained = inputNodeHelper.isInMethodChained();
			allowInMethodResultCast = inputNodeHelper.isInMethodCast();
			inMethodUnchecked = inputNodeHelper.isInMethodUnchecked();
			inMethod = inputNodeHelper.getInMethod() != null ? inputNodeHelper
					.getInMethod().getExecutable() : null;
			inMethodName = inputNodeHelper.getInMethodName();
			preInputType = inputNodeHelper.getPreInputType();
			postInputType = inputNodeHelper.getPostInputType();
		}

		private boolean hasOutMethod(
				OverrideMerge<S, ? extends BindingChildNodeConfiguratorImpl<?, S, ?>> overrideMerge) {
			return !"null".equals(outMethodName)
					&& !(overrideMerge.configurator().getContext().isAbstract()
							&& isAbstract() && outMethodName == null);
		}

		@Override
		public TypeToken<?> getPreInputType() {
			return preInputType;
		}

		@Override
		public TypeToken<?> getPostInputType() {
			return postInputType;
		}

		@Override
		public Boolean isOrdered() {
			return ordered;
		}

		@Override
		public final Boolean isExtensible() {
			return extensible;
		}

		@Override
		public final Range<Integer> occurrences() {
			return occurrences;
		}

		@Override
		public final String getOutMethodName() {
			return outMethodName;
		}

		@Override
		public final Method getOutMethod() {
			return outMethod;
		}

		@Override
		public final Boolean isOutMethodIterable() {
			return iterable;
		}

		@Override
		public Boolean isOutMethodUnchecked() {
			return outMethodUnchecked;
		}

		@Override
		public Boolean isOutMethodCast() {
			return outMethodCast;
		}

		@Override
		public final String getInMethodName() {
			return inMethodName;
		}

		@Override
		public final Executable getInMethod() {
			return inMethod;
		}

		@Override
		public final Boolean isInMethodChained() {
			return inMethodChained;
		}

		@Override
		public Boolean isInMethodCast() {
			return allowInMethodResultCast;
		}

		@Override
		public Boolean isInMethodUnchecked() {
			return inMethodUnchecked;
		}

		@SuppressWarnings("unchecked")
		private static <U> TypeToken<Iterable<? extends U>> getIteratorType(
				TypeToken<U> type) {
			if (type == null) {
				type = (TypeToken<U>) new TypeToken<Object>() {};
			}
			return new TypeToken<Iterable<? extends U>>() {}.withTypeArgument(
					new TypeParameter<U>() {}, type);
		}

		protected static Invokable<?, ?> getOutMethod(
				BindingChildNode.Effective<?, ?, ?> node, Method inheritedOutMethod,
				TypeToken<?> receiverType, BoundSet bounds) {

			try {
				boolean outMethodCast = node.isOutMethodCast() != null
						&& node.isOutMethodCast();

				TypeToken<?> resultType = ((node.isOutMethodIterable() != null && node
						.isOutMethodIterable()) ? getIteratorType((TypeToken<?>) (outMethodCast ? TypeToken
						.over(new InferenceVariable()) : node.getDataType()))
						: node.getDataType());

				if (node.isOutMethodUnchecked() != null && node.isOutMethodUnchecked())
					resultType = TypeToken.over(resultType.getRawType());

				Invokable<?, ?> outMethod;
				if ("this".equals(node.getOutMethodName())) {
					if (!resultType.isAssignableFrom(receiverType)) {
						throw new SchemaException("Can't use out method 'this' for node '"
								+ node.getName() + "', as result class '" + resultType
								+ "' cannot be assigned from target class'" + receiverType
								+ "'");
					}

					outMethod = null;

					ConstraintFormula.reduce(Kind.LOOSE_COMPATIBILILTY,
							receiverType.getType(), resultType.getType(), bounds);
				} else if (receiverType == null) {
					throw new SchemaException("Can't find out method for node '"
							+ node.getName() + "' as target class cannot be found");
				} else if (resultType == null) {
					throw new SchemaException("Can't find out method for node '"
							+ node.getName() + "' as result class cannot be found");
				} else if (inheritedOutMethod != null) {
					try {
						outMethod = Invokable.over(inheritedOutMethod, receiverType)
								.withLooseApplicability();
					} catch (Exception e) {
						outMethod = Invokable.over(inheritedOutMethod, receiverType)
								.withVariableArityApplicability();
					}

					if (outMethodCast) {
						// TODO enforce castability
					} else {
						outMethod = outMethod.withTargetType(resultType);
					}
				} else {
					outMethod = Methods.findMethod(
							generateOutMethodNames(node, resultType.getRawType()),
							receiverType, false, resultType, outMethodCast);
				}

				if (outMethod != null)
					bounds.incorporate(outMethod.getResolver().getBounds());

				return outMethod;
			} catch (NoSuchMethodException e) {
				throw new SchemaException(e);
			}
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

		@SuppressWarnings("rawtypes")
		protected static final PropertySet<BindingChildNode.Effective> PROPERTY_SET = new PropertySet<>(
				BindingChildNode.Effective.class)
				.add(BindingChildNodeImpl.PROPERTY_SET)
				.add(BindingNodeImpl.Effective.PROPERTY_SET)
				.add(BindingChildNode.Effective::getOutMethod)
				.add(InputNode.Effective::getInMethod);

		@Override
		protected PropertySet<? super E> effectivePropertySet() {
			return PROPERTY_SET;
		}
	}

	private final TypeToken<?> postInputClass;

	private final Range<Integer> occurrences;

	private final Boolean iterable;
	private final Boolean outMethodUnchecked;
	private final Boolean outMethodCast;
	private final String outMethodName;

	private final String inMethodName;
	private final Boolean inMethodChained;
	private final Boolean allowInMethodResultCast;
	private final Boolean inMethodUnchecked;

	private final Boolean extensible;
	private final Boolean ordered;

	BindingChildNodeImpl(BindingChildNodeConfiguratorImpl<?, ?, T> configurator) {
		super(configurator);

		postInputClass = configurator.getPostInputClass();

		extensible = configurator.getExtensible();
		ordered = configurator.getOrdered();
		occurrences = configurator.getOccurrences();
		iterable = configurator.getOutMethodIterable();
		outMethodUnchecked = configurator.getOutMethodUnchecked();
		outMethodCast = configurator.getOutMethodCast();
		outMethodName = configurator.getOutMethodName();

		inMethodName = configurator.getInMethodName();
		inMethodChained = configurator.getInMethodChained();
		allowInMethodResultCast = configurator.getInMethodCast();
		inMethodUnchecked = configurator.getInMethodUnchecked();
	}

	@SuppressWarnings("rawtypes")
	protected static final PropertySet<BindingChildNode> PROPERTY_SET = new PropertySet<>(
			BindingChildNode.class).add(BindingNodeImpl.PROPERTY_SET)
			.add(BindingChildNode::getOutMethodName)
			.add(BindingChildNode::isOutMethodIterable)
			.add(BindingChildNode::isOutMethodUnchecked)
			.add(BindingChildNode::isOutMethodCast)
			.add(BindingChildNode::occurrences).add(BindingChildNode::isOrdered)
			.add(BindingChildNode::isExtensible).add(InputNode::getInMethodName)
			.add(InputNode::isInMethodChained).add(InputNode::isInMethodCast);

	@Override
	protected PropertySet<? super S> propertySet() {
		return PROPERTY_SET;
	}

	@Override
	public Boolean isOrdered() {
		return ordered;
	}

	@Override
	public final Boolean isExtensible() {
		return extensible;
	}

	@Override
	public final Range<Integer> occurrences() {
		return occurrences;
	}

	@Override
	public final String getOutMethodName() {
		return outMethodName;
	}

	@Override
	public final Boolean isOutMethodIterable() {
		return iterable;
	}

	@Override
	public Boolean isOutMethodUnchecked() {
		return outMethodUnchecked;
	}

	@Override
	public Boolean isOutMethodCast() {
		return outMethodCast;
	}

	@Override
	public final String getInMethodName() {
		return inMethodName;
	}

	@Override
	public final Boolean isInMethodChained() {
		return inMethodChained;
	}

	@Override
	public Boolean isInMethodCast() {
		return allowInMethodResultCast;
	}

	@Override
	public Boolean isInMethodUnchecked() {
		return inMethodUnchecked;
	}

	@Override
	public TypeToken<?> getPostInputType() {
		return postInputClass;
	}
}
