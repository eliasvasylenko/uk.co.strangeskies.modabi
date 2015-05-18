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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.BindingChildNode;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.modabi.schema.node.building.configuration.BindingChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.Methods;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.ConstraintFormula;
import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.reflection.Invokable;
import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;

public abstract class BindingChildNodeConfiguratorImpl<S extends BindingChildNodeConfigurator<S, N, T>, N extends BindingChildNode<T, N, ?>, T>
		extends BindingNodeConfiguratorImpl<S, N, T> implements
		BindingChildNodeConfigurator<S, N, T> {
	protected static abstract class BindingChildNodeImpl<T, S extends BindingChildNode<T, S, E>, E extends BindingChildNode.Effective<T, S, E>>
			extends BindingNodeImpl<T, S, E> implements BindingChildNode<T, S, E> {
		protected static abstract class Effective<T, S extends BindingChildNode<T, S, E>, E extends BindingChildNode.Effective<T, S, E>>
				extends BindingNodeImpl.Effective<T, S, E> implements
				BindingChildNode.Effective<T, S, E> {
			private final Range<Integer> occurrences;

			private final Boolean iterable;
			private String outMethodName;
			private final Method outMethod;
			private final Boolean outMethodUnchecked;

			private String inMethodName;
			private final Executable inMethod;
			private final Boolean inMethodChained;
			private final Boolean allowInMethodResultCast;
			private final Boolean inMethodUnchecked;

			private final Boolean extensible;
			private final Boolean ordered;

			private final Type preInputClass;
			private final Type postInputClass;

			protected Effective(
					OverrideMerge<S, ? extends BindingChildNodeConfiguratorImpl<?, S, ?>> overrideMerge) {
				super(overrideMerge);

				if (isAbstract()
						&& !overrideMerge.configurator().getContext().isAbstract())
					throw new SchemaException(
							"Node '"
									+ getName()
									+ "' has no abstract or extensible parents, so cannot be abstract.");

				extensible = overrideMerge.getValue(BindingChildNode::isExtensible,
						false);

				ordered = overrideMerge.getValue(BindingChildNode::isOrdered, true);

				occurrences = overrideMerge.getValue(BindingChildNode::occurrences, (v,
						o) -> o.contains(v), Range.create(1, 1));

				iterable = overrideMerge.getValue(
						BindingChildNode::isOutMethodIterable, false);

				outMethodUnchecked = overrideMerge.getValue(
						BindingChildNode::isOutMethodUnchecked, false);

				outMethodName = overrideMerge
						.tryGetValue(BindingChildNode::getOutMethodName);

				Method overriddenOutMethod = overrideMerge.tryGetValue(n -> n
						.effective() == null ? null : n.effective().getOutMethod());

				Invokable<?, ?> outInvokable = (isAbstract() || "null"
						.equals(outMethodName)) ? null : getOutMethod(this,
						overriddenOutMethod, overrideMerge.configurator().getContext()
								.outputSourceType(), overrideMerge.configurator().getContext()
								.boundSet());

				outMethod = outInvokable == null ? null : (Method) outInvokable
						.getExecutable();

				if (outMethodName == null && !isAbstract())
					outMethodName = outMethod.getName();

				InputNodeConfigurationHelper<S, E> inputNodeHelper = new InputNodeConfigurationHelper<S, E>(
						isAbstract(), getName(), overrideMerge, overrideMerge
								.configurator().getContext(), Arrays.asList(getExactDataType()));

				inMethodChained = inputNodeHelper.isInMethodChained();
				allowInMethodResultCast = inputNodeHelper.isInMethodCast();
				inMethodUnchecked = inputNodeHelper.isInMethodUnchecked();
				inMethod = inputNodeHelper.getInMethod() != null ? inputNodeHelper
						.getInMethod().getExecutable() : null;
				inMethodName = inputNodeHelper.getInMethodName();
				preInputClass = inputNodeHelper.getPreInputType();
				postInputClass = inputNodeHelper.getPostInputType();
			}

			@Override
			public Type getPreInputType() {
				return preInputClass;
			}

			@Override
			public Type getPostInputType() {
				return postInputClass;
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

			private static <U> TypeToken<Iterable<? extends U>> getIteratorType(
					TypeToken<U> type) {
				return new TypeToken<Iterable<? extends U>>() {}.withTypeArgument(
						new TypeParameter<U>() {}, type);
			}

			protected static Invokable<?, ?> getOutMethod(
					BindingChildNodeImpl.Effective<?, ?, ?> node,
					Method inheritedOutMethod, TypeToken<?> receiverType, BoundSet bounds) {
				/*
				 * TODO
				 * 
				 * 
				 * 
				 * 
				 * 
				 * 
				 * TODO When overriding nodes with an existing outMethod, don't bother
				 * trying to resolve the overload again!!!!!! Just use it and check for
				 * applicability.
				 */
				try {
					TypeToken<?> resultClass = ((node.isOutMethodIterable() != null && node
							.isOutMethodIterable()) ? getIteratorType(node.getExactDataType())
							: node.getExactDataType());

					if (node.isOutMethodUnchecked())
						resultClass = TypeToken.over(resultClass.getRawType());

					Invokable<?, ?> outMethod;
					if (node.getOutMethodName() != null
							&& node.getOutMethodName().equals("this")) {
						if (!resultClass.isAssignableFrom(receiverType)
								&& !resultClass.getRawType().isAssignableFrom(
										receiverType.getRawType())
								&& !resultClass.isContainedBy(receiverType
										.resolveSupertypeParameters(resultClass.getRawType())))
							throw new SchemaException(
									"Can't use out method 'this' for node '" + node.getName()
											+ "', as result class '" + resultClass
											+ "' cannot be assigned from target class'"
											+ receiverType + "'.");
						outMethod = null;

						ConstraintFormula.reduce(Kind.LOOSE_COMPATIBILILTY,
								receiverType.getType(), resultClass.getType(), bounds);
					} else if (receiverType == null) {
						if (!node.isAbstract())
							throw new SchemaException("Can't find out method for node '"
									+ node.getName() + "' as target class cannot be found.");
						outMethod = null;
					} else if (resultClass == null) {
						if (!node.isAbstract())
							throw new SchemaException("Can't find out method for node '"
									+ node.getName() + "' as result class cannot be found.");
						outMethod = null;
					} else {
						outMethod = Methods.findMethod(
								generateOutMethodNames(node, resultClass.getRawType()),
								receiverType, false, resultClass, false);

						if (inheritedOutMethod != null
								&& !outMethod.getExecutable().equals(inheritedOutMethod))
							throw new SchemaException();
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
		}

		private final Type postInputClass;

		private final Range<Integer> occurrences;

		private final Boolean iterable;
		private final Boolean outMethodUnchecked;
		private final String outMethodName;

		private final String inMethodName;
		private final Boolean inMethodChained;
		private final Boolean allowInMethodResultCast;
		private final Boolean inMethodUnchecked;

		private final Boolean extensible;
		private final Boolean ordered;

		BindingChildNodeImpl(BindingChildNodeConfiguratorImpl<?, ?, T> configurator) {
			super(configurator);

			postInputClass = configurator.postInputClass;

			extensible = configurator.extensible;
			ordered = configurator.ordered;
			occurrences = configurator.occurrences;
			iterable = configurator.iterable;
			outMethodUnchecked = configurator.outMethodUnchecked;
			outMethodName = configurator.outMethodName;

			inMethodName = configurator.inMethodName;
			inMethodChained = configurator.inMethodChained;
			allowInMethodResultCast = configurator.allowInMethodResultCast;
			inMethodUnchecked = configurator.inMethodUnchecked;
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
		public Type getPostInputType() {
			return postInputClass;
		}
	}

	private final SchemaNodeConfigurationContext<? super N> context;

	private Type postInputClass;
	private Range<Integer> occurrences;
	private Boolean iterable;
	private Boolean outMethodUnchecked;
	private String outMethodName;
	private String inMethodName;
	private Boolean inMethodChained;
	private Boolean allowInMethodResultCast;
	private Boolean inMethodUnchecked;
	private Boolean extensible;
	private Boolean ordered;

	public BindingChildNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super N> parent) {
		this.context = parent;

		addResultListener(result -> parent.addChild(result));
	}

	protected final SchemaNodeConfigurationContext<? super N> getContext() {
		return context;
	}

	@Override
	protected Namespace getNamespace() {
		return getName() != null ? getName().getNamespace() : getContext()
				.namespace();
	}

	@Override
	protected DataLoader getDataLoader() {
		return getContext().dataLoader();
	}

	@Override
	public <V extends T> BindingChildNodeConfigurator<?, ?, V> dataType(
			TypeToken<V> dataClass) {
		return (BindingChildNodeConfigurator<?, ?, V>) super.dataType(dataClass);
	}

	@Override
	public final S occurrences(Range<Integer> range) {
		assertConfigurable(occurrences);
		occurrences = range;
		return getThis();
	}

	@Override
	public final S inMethod(String inMethodName) {
		if (!getContext().isInputExpected() && !inMethodName.equals("null"))
			throw new SchemaException(
					"No input method should be specified on this node.");

		assertConfigurable(this.inMethodName);
		this.inMethodName = inMethodName;

		return getThis();
	}

	@Override
	public final S inMethodChained(boolean chained) {
		assertConfigurable(this.inMethodChained);
		this.inMethodChained = chained;
		return getThis();
	}

	@Override
	public final S inMethodCast(boolean allowInMethodResultCast) {
		assertConfigurable(this.allowInMethodResultCast);
		this.allowInMethodResultCast = allowInMethodResultCast;

		return getThis();
	}

	@Override
	public final S inMethodUnchecked(boolean unchecked) {
		assertConfigurable(inMethodUnchecked);
		inMethodUnchecked = unchecked;

		return getThis();
	}

	@Override
	public final S outMethod(String outMethodName) {
		assertConfigurable(this.outMethodName);
		this.outMethodName = outMethodName;
		return getThis();
	}

	@Override
	public final S outMethodIterable(boolean iterable) {
		assertConfigurable(this.iterable);
		this.iterable = iterable;
		return getThis();
	}

	@Override
	public S outMethodUnchecked(boolean unchecked) {
		assertConfigurable(this.outMethodUnchecked);
		this.outMethodUnchecked = unchecked;
		return getThis();
	}

	@Override
	public final S extensible(boolean extensible) {
		assertConfigurable(this.extensible);
		this.extensible = extensible;

		return getThis();
	}

	@Override
	public final S ordered(boolean ordered) {
		assertConfigurable(this.ordered);
		this.ordered = ordered;

		return getThis();
	}

	@Override
	public List<N> getOverriddenNodes() {
		return getName() == null ? Collections.emptyList() : getContext()
				.overrideChild(getName(), getNodeClass());
	}

	@Override
	protected final boolean isChildContextAbstract() {
		return super.isChildContextAbstract() || getContext().isAbstract()
				|| extensible != null && extensible;
	}

	@Override
	public S postInputType(Type postInputClass) {
		assertConfigurable(this.postInputClass);
		this.postInputClass = postInputClass;

		return getThis();
	}
}
