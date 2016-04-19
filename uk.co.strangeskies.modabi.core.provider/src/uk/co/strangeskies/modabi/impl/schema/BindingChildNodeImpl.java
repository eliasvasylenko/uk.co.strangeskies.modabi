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
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.impl.schema.utilities.Methods;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.BindingChildNode;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.ConstraintFormula;
import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.reflection.InferenceVariable;
import uk.co.strangeskies.reflection.Invokable;
import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;

abstract class BindingChildNodeImpl<T, S extends BindingChildNode<T, S, E>, E extends BindingChildNode.Effective<T, S, E>>
		extends BindingNodeImpl<T, S, E> implements BindingChildNode<T, S, E> {
	protected static abstract class Effective<T, S extends BindingChildNode<T, S, E>, E extends BindingChildNode.Effective<T, S, E>>
			extends BindingNodeImpl.Effective<T, S, E> implements BindingChildNode.Effective<T, S, E> {
		private final SchemaNode.Effective<?, ?> parent;

		private final Boolean nullIfOmitted;
		private final Range<Integer> occurrences;
		private final Boolean ordered;

		private final Boolean iterable;
		private String outMethodName;
		private Invokable<?, ?> outMethod;
		private final Boolean outMethodUnchecked;
		private final Boolean outMethodCast;

		private String inMethodName;
		private Invokable<?, ?> inMethod;
		private Boolean inMethodChained;
		private Boolean allowInMethodResultCast;
		private Boolean inMethodUnchecked;

		private TypeToken<?> preInputType;
		private TypeToken<?> postInputType;

		private final Boolean extensible;

		protected Effective(OverrideMerge<S, ? extends BindingChildNodeConfiguratorImpl<?, S, T>> overrideMerge) {
			this(overrideMerge, true);
		}

		protected Effective(OverrideMerge<S, ? extends BindingChildNodeConfiguratorImpl<?, S, T>> overrideMerge,
				boolean integrateIO) {
			super(overrideMerge);

			parent = overrideMerge.configurator().getContext().parentNodeProxy().effective();

			extensible = overrideMerge.node().isExtensible() == null ? false : overrideMerge.node().isExtensible();

			if (abstractness().isMoreThan(Abstractness.UNINFERRED) && !overrideMerge.configurator().getContext().isAbstract()
					&& !isExtensible())
				throw new SchemaException(
						"Node '" + name() + "' has no abstract or extensible parents, so cannot be abstract.");

			ordered = overrideMerge.getOverride(BindingChildNode::isOrdered).orDefault(true).get();

			occurrences = overrideMerge.getOverride(BindingChildNode::occurrences).validate((v, o) -> o.contains(v))
					.orDefault(Range.between(1, 1)).get();

			iterable = overrideMerge.getOverride(n -> {
				if (n.isOutMethodIterable() != null) {
					return n.isOutMethodIterable();
				}

				if (n.occurrences() != null && !n.occurrences().isValueAbove(2)) {
					return true;
				}

				return null;
			}).orDefault(false).get();

			outMethodUnchecked = overrideMerge.getOverride(BindingChildNode::isOutMethodUnchecked).orDefault(false).get();

			outMethodCast = overrideMerge.getOverride(BindingChildNode::isOutMethodCast).orDefault(false).get();

			outMethodName = overrideMerge.getOverride(BindingChildNode::getOutMethodName).tryGet();

			/*
			 * Determine effective 'null if omitted' property. Must be true for nodes
			 * which form part of an inputSequence, or which bind their data into a
			 * constructor or static factory.
			 * 
			 * TODO or maybe it should throw an exception in those cases if not true?
			 */
			boolean mustBeNullIfOmitted = !overrideMerge.configurator().getContext().isInputExpected()
					|| overrideMerge.configurator().getContext().isConstructorExpected()
					|| overrideMerge.configurator().getContext().isStaticMethodExpected();
			nullIfOmitted = overrideMerge.getOverride(BindingChildNode::nullIfOmitted).validate((n, o) -> o || !n)
					.orDefault(mustBeNullIfOmitted).get();
			if (nullIfOmitted != null && !nullIfOmitted && mustBeNullIfOmitted) {
				throw new SchemaException("'Null if omitted' property must be true for node '" + name() + "'");
			}

			if (integrateIO) {
				integrateIO(overrideMerge);
			}
		}

		public void integrateIO(OverrideMerge<S, ? extends BindingChildNodeConfiguratorImpl<?, S, T>> overrideMerge) {
			Method overriddenOutMethod = (Method) overrideMerge.getOverride(n -> {
				if (n.effective() == null)
					return null;

				if (n.effective().getOutMethod() == null)
					return null;

				return n.effective().getOutMethod().getExecutable();
			}).tryGet();

			outMethod = hasOutMethod(overrideMerge)
					? getOutMethod(this, overriddenOutMethod, overrideMerge.configurator().getContext().outputSourceType(),
							overrideMerge.configurator().getContext().boundSet())
					: null;

			if (outMethodName == null && hasOutMethod(overrideMerge))
				outMethodName = outMethod.getExecutable().getName();

			InputNodeConfigurationHelper<S, E> inputNodeHelper = new InputNodeConfigurationHelper<>(abstractness(), name(),
					overrideMerge, overrideMerge.configurator().getContext(), Arrays.asList(getDataType()));

			inMethodChained = inputNodeHelper.isInMethodChained();
			allowInMethodResultCast = inputNodeHelper.isInMethodCast();
			inMethodUnchecked = inputNodeHelper.isInMethodUnchecked();
			inMethod = inputNodeHelper.getInMethod();
			inMethodName = inputNodeHelper.getInMethodName();
			preInputType = inputNodeHelper.getPreInputType();
			postInputType = inputNodeHelper.getPostInputType();
		}

		@Override
		public final Boolean nullIfOmitted() {
			return nullIfOmitted;
		}

		@Override
		public SchemaNode.Effective<?, ?> parent() {
			return parent;
		}

		private boolean hasOutMethod(OverrideMerge<S, ? extends BindingChildNodeConfiguratorImpl<?, S, ?>> overrideMerge) {
			return !"null".equals(outMethodName) && !(overrideMerge.configurator().getContext().isAbstract()
					&& abstractness().isMoreThan(Abstractness.RESOLVED)
					&& (outMethodName == null || "this".equals(outMethodName)));
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
		public final Invokable<?, ?> getOutMethod() {
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
		public final Invokable<?, ?> getInMethod() {
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
		private static <U> TypeToken<Iterable<? extends U>> getIteratorType(BindingChildNode.Effective<U, ?, ?> node) {
			boolean outMethodCast = node.isOutMethodCast() != null && node.isOutMethodCast();

			TypeToken<U> type = outMethodCast ? (TypeToken<U>) TypeToken.over(new InferenceVariable()) : node.getDataType();
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

		protected static Invokable<?, ?> getOutMethod(BindingChildNode.Effective<?, ?, ?> node, Method inheritedOutMethod,
				TypeToken<?> receiverType, BoundSet bounds) {
			if (receiverType == null) {
				throw new SchemaException(
						"Can't find out method for node '" + node.name() + "' as target class cannot be found");
			}

			boolean outMethodCast = node.isOutMethodCast() != null && node.isOutMethodCast();

			TypeToken<?> resultType = ((node.isOutMethodIterable() != null && node.isOutMethodIterable())
					? getIteratorType(node) : node.getDataType());

			if (node.isOutMethodUnchecked() != null && node.isOutMethodUnchecked())
				resultType = TypeToken.over(resultType.getRawType());

			if (resultType == null)
				throw new SchemaException(
						"Can't find out method for node '" + node.name() + "' as result class cannot be found");

			Invokable<?, ?> outMethod;
			if ("this".equals(node.getOutMethodName())) {
				if (!resultType.isAssignableFrom(receiverType.resolve())) {
					throw new SchemaException("Can't use out method 'this' for node '" + node.name() + "', as result class '"
							+ resultType + "' cannot be assigned from target class '" + receiverType + "'");
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
				try {
					outMethod = Methods.findMethod(generateOutMethodNames(node, resultType.getRawType()), receiverType, false,
							resultType, outMethodCast);
				} catch (NoSuchMethodException e) {
					throw new SchemaException(e);
				}
			}

			if (outMethod != null) {
				bounds.incorporate(outMethod.getResolver().getBounds());
			}

			return outMethod;
		}

		private static List<String> generateOutMethodNames(BindingChildNode.Effective<?, ?, ?> node, Class<?> resultClass) {
			List<String> names;

			if (node.getOutMethodName() != null)
				names = Arrays.asList(node.getOutMethodName());
			else
				names = generateUnbindingMethodNames(node.name().getName(),
						node.isOutMethodIterable() != null && node.isOutMethodIterable(), resultClass);

			return names;
		}

		@Override
		public boolean equals(Object that) {
			return super.equals(that) && that instanceof ChildNode.Effective
					&& Objects.equals(parent(), ((ChildNode.Effective<?, ?>) that).parent());
		}

		@Override
		public final int hashCode() {
			return super.hashCode() ^ Objects.hashCode(parent());
		}
	}

	private final SchemaNode<?, ?> parent;

	private final TypeToken<?> postInputClass;

	private final Range<Integer> occurrences;
	private final Boolean nullIfOmitted;

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

		parent = configurator.getContext().parentNodeProxy();

		postInputClass = configurator.getPostInputClass();

		extensible = configurator.getExtensible();
		ordered = configurator.getOrdered();
		occurrences = configurator.getOccurrences();
		nullIfOmitted = configurator.getNullIfOmitted();

		iterable = configurator.getOutMethodIterable();
		outMethodUnchecked = configurator.getOutMethodUnchecked();
		outMethodCast = configurator.getOutMethodCast();
		outMethodName = configurator.getOutMethodName();

		inMethodName = configurator.getInMethodName();
		inMethodChained = configurator.getInMethodChained();
		allowInMethodResultCast = configurator.getInMethodCast();
		inMethodUnchecked = configurator.getInMethodUnchecked();
	}

	@Override
	public final Boolean nullIfOmitted() {
		return nullIfOmitted;
	}

	@Override
	protected Boolean isExplicitlyExtensible() {
		return extensible != null && extensible;
	}

	@Override
	public SchemaNode<?, ?> parent() {
		return parent;
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

	@Override
	public boolean equals(Object that) {
		return super.equals(that) && that instanceof ChildNode
				&& Objects.equals(parent(), ((ChildNode<?, ?>) that).parent());
	}

	@Override
	public final int hashCode() {
		return super.hashCode() ^ Objects.hashCode(parent());
	}
}
