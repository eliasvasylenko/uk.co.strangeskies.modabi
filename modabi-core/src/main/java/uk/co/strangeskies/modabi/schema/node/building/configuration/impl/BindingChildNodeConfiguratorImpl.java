package uk.co.strangeskies.modabi.schema.node.building.configuration.impl;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ClassUtils;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.BindingChildNode;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.modabi.schema.node.building.configuration.BindingChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.Methods;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.SchemaNodeConfigurationContext;

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

			private String inMethodName;
			private final Executable inMethod;
			private final Boolean inMethodChained;
			private final Boolean allowInMethodResultCast;

			private final Boolean extensible;
			private final Boolean ordered;

			private final Class<?> preInputClass;
			private final Class<?> postInputClass;

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

				outMethodName = overrideMerge
						.tryGetValue(BindingChildNode::getOutMethodName);

				Method overriddenOutMethod = overrideMerge.tryGetValue(n -> n
						.effective() == null ? null : n.effective().getOutMethod());
				outMethod = (isAbstract() || "null".equals(outMethodName)) ? null
						: getOutMethod(this, overriddenOutMethod, overrideMerge
								.configurator().getContext().outputSourceClass());

				if (outMethodName == null && !isAbstract())
					outMethodName = outMethod.getName();

				InputNodeConfigurationHelper<S, E> inputNodeHelper = new InputNodeConfigurationHelper<S, E>(
						effective(), overrideMerge, overrideMerge.configurator()
								.getContext());
				inMethodChained = inputNodeHelper.isInMethodChained();
				allowInMethodResultCast = inputNodeHelper.isInMethodCast();
				inMethod = inputNodeHelper.inMethod(Arrays.asList(getDataClass()));
				inMethodName = inputNodeHelper.inMethodName();
				preInputClass = inputNodeHelper.preInputClass();
				postInputClass = inputNodeHelper.postInputClass();
			}

			@Override
			public Class<?> getPreInputClass() {
				return preInputClass;
			}

			@Override
			public Class<?> getPostInputClass() {
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

			public static Method getOutMethod(
					BindingChildNode.Effective<?, ?, ?> node, Method inheritedOutMethod,
					Class<?> targetClass) {
				try {
					Class<?> resultClass = (node.isOutMethodIterable() != null && node
							.isOutMethodIterable()) ? Iterable.class : node.getDataClass();

					Method outMethod;
					if (node.getOutMethodName() != null
							&& node.getOutMethodName().equals("this")) {
						if (!ClassUtils.isAssignable(targetClass, resultClass))
							throw new SchemaException(
									"Can't use out method 'this' for node '" + node.getName()
											+ "', as result class '" + resultClass
											+ "' cannot be assigned from target class'" + targetClass
											+ "'.");
						outMethod = null;
					} else if (targetClass == null) {
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
								generateOutMethodNames(node, resultClass), targetClass, false,
								resultClass, false);

						if (inheritedOutMethod != null
								&& !outMethod.equals(inheritedOutMethod))
							throw new SchemaException();
					}

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

		private final Class<?> postInputClass;

		private final Range<Integer> occurrences;

		private final Boolean iterable;
		private final String outMethodName;

		private final String inMethodName;
		private final Boolean inMethodChained;
		private final Boolean allowInMethodResultCast;

		private final Boolean extensible;
		private final Boolean ordered;

		BindingChildNodeImpl(BindingChildNodeConfiguratorImpl<?, ?, T> configurator) {
			super(configurator);

			postInputClass = configurator.postInputClass;

			extensible = configurator.extensible;
			ordered = configurator.ordered;
			occurrences = configurator.occurrences;
			iterable = configurator.iterable;
			outMethodName = configurator.outMethodName;

			inMethodName = configurator.inMethodName;
			inMethodChained = configurator.inMethodChained;
			allowInMethodResultCast = configurator.allowInMethodResultCast;
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
		public Class<?> getPostInputClass() {
			return postInputClass;
		}
	}

	private final SchemaNodeConfigurationContext<? super N> context;

	private Class<?> postInputClass;
	private Range<Integer> occurrences;
	private Boolean iterable;
	private String outMethodName;
	private String inMethodName;
	private Boolean inMethodChained;
	private Boolean allowInMethodResultCast;
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
	public <V extends T> BindingChildNodeConfigurator<?, ?, V> dataClass(
			Class<V> dataClass) {
		return (BindingChildNodeConfigurator<?, ?, V>) super.dataClass(dataClass);
	}

	@Override
	public final S occurrences(Range<Integer> range) {
		requireConfigurable(occurrences);
		occurrences = range;
		return getThis();
	}

	@Override
	public final S inMethod(String inMethodName) {
		if (!getContext().isInputExpected() && !inMethodName.equals("null"))
			throw new SchemaException(
					"No input method should be specified on this node.");

		requireConfigurable(this.inMethodName);
		this.inMethodName = inMethodName;

		return getThis();
	}

	@Override
	public final S inMethodChained(boolean chained) {
		requireConfigurable(this.inMethodChained);
		this.inMethodChained = chained;
		return getThis();
	}

	@Override
	public final S isInMethodCast(boolean allowInMethodResultCast) {
		requireConfigurable(this.allowInMethodResultCast);
		this.allowInMethodResultCast = allowInMethodResultCast;

		return getThis();
	}

	@Override
	public final S outMethod(String outMethodName) {
		requireConfigurable(this.outMethodName);
		this.outMethodName = outMethodName;
		return getThis();
	}

	@Override
	public final S outMethodIterable(boolean iterable) {
		requireConfigurable(this.iterable);
		this.iterable = iterable;
		return getThis();
	}

	@Override
	public final S extensible(boolean extensible) {
		requireConfigurable(this.extensible);
		this.extensible = extensible;

		return getThis();
	}

	@Override
	public final S ordered(boolean ordered) {
		requireConfigurable(this.ordered);
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
	public S postInputClass(Class<?> postInputClass) {
		requireConfigurable(this.postInputClass);
		this.postInputClass = postInputClass;

		return getThis();
	}
}
