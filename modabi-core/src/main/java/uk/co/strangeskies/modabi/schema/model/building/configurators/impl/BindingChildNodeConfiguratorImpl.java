package uk.co.strangeskies.modabi.schema.model.building.configurators.impl;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.building.DataLoader;
import uk.co.strangeskies.modabi.schema.model.building.configurators.BindingChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.impl.ChildNodeImpl;
import uk.co.strangeskies.modabi.schema.model.building.impl.Methods;
import uk.co.strangeskies.modabi.schema.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.schema.model.building.impl.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;

public abstract class BindingChildNodeConfiguratorImpl<S extends BindingChildNodeConfigurator<S, N, T, C, B>, N extends BindingChildNode<T, N, ?>, T, C extends ChildNode<?, ?>, B extends BindingChildNode<?, ?, ?>>
		extends BindingNodeConfiguratorImpl<S, N, T, C, B> implements
		BindingChildNodeConfigurator<S, N, T, C, B> {
	protected static abstract class BindingChildNodeImpl<T, S extends BindingChildNode<T, S, E>, E extends BindingChildNode.Effective<T, S, E>>
			extends BindingNodeImpl<T, S, E> implements ChildNodeImpl<S, E>,
			BindingChildNode<T, S, E> {
		protected static abstract class Effective<T, S extends BindingChildNode<T, S, E>, E extends BindingChildNode.Effective<T, S, E>>
				extends BindingNodeImpl.Effective<T, S, E> implements
				BindingChildNode.Effective<T, S, E> {
			private final Range<Integer> occurances;

			private final Boolean iterable;
			private final String outMethodName;
			private final Method outMethod;

			private final String inMethodName;
			private final Method inMethod;
			private final Boolean inMethodChained;

			private final Boolean extensible;
			private final Boolean ordered;

			private final Class<?> preInputClass;
			private final Class<?> postInputClass;

			protected Effective(
					OverrideMerge<S, ? extends BindingChildNodeConfiguratorImpl<?, ?, ?, ?, ?>> overrideMerge) {
				super(overrideMerge);

				extensible = overrideMerge.getValue(BindingChildNode::isExtensible);

				if (isAbstract()
						&& !overrideMerge.configurator().getContext().isAbstract()
						&& !(isExtensible() != null && isExtensible()))
					throw new SchemaException(
							"Node '"
									+ getName()
									+ "' is not extensible and has no abstract parents, so cannot be abstract.");

				ordered = overrideMerge.getValue(BindingChildNode::isOrdered, true);

				occurances = overrideMerge.getValue(BindingChildNode::occurances,
						(v, o) -> o.contains(v), Range.create(1, 1));

				iterable = overrideMerge.getValue(
						BindingChildNode::isOutMethodIterable, false);

				outMethodName = overrideMerge
						.getValue(BindingChildNode::getOutMethodName);

				inMethodName = overrideMerge
						.getValue(BindingChildNode::getInMethodName);

				inMethodChained = overrideMerge.getValue(
						BindingChildNode::isInMethodChained, false);

				Method overriddenOutMethod = overrideMerge
						.getValue(n -> n.effective() == null ? null : n.effective()
								.getOutMethod());

				outMethod = (isAbstract() || "null".equals(outMethodName)) ? null
						: Methods.getOutMethod(this, overriddenOutMethod, overrideMerge
								.configurator().getContext().getOutputSourceClass());

				Method overriddenInMethod = overrideMerge
						.getValue(n -> n.effective() == null ? null : n.effective()
								.getInMethod());

				inMethod = (isAbstract() || "null".equals(inMethodName)) ? null
						: Methods.getInMethod(this, overriddenInMethod, overrideMerge
								.configurator().getContext().getInputTargetClass(getName()),
								Arrays.asList(getDataClass()));

				preInputClass = (isAbstract() || "null".equals(inMethodName)) ? null
						: inMethod.getDeclaringClass();

				postInputClass = (isAbstract() || "null".equals(inMethodName)) ? null
						: !isInMethodChained() ? getPreInputClass() : inMethod
								.getReturnType();
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
			public final Range<Integer> occurances() {
				return occurances;
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
			public final Method getInMethod() {
				return inMethod;
			}

			@Override
			public final Boolean isInMethodChained() {
				return inMethodChained;
			}
		}

		private final Class<?> postInputClass;

		private final Range<Integer> occurances;

		private final Boolean iterable;
		private final String outMethodName;

		private final String inMethodName;
		private final Boolean inMethodChained;

		private final Boolean extensible;
		private final Boolean ordered;

		BindingChildNodeImpl(
				BindingChildNodeConfiguratorImpl<?, ?, T, ?, ?> configurator) {
			super(configurator);

			postInputClass = configurator.postInputClass;

			extensible = configurator.extensible;
			ordered = configurator.ordered;
			occurances = configurator.occurances;
			iterable = configurator.iterable;
			outMethodName = configurator.outMethodName;

			inMethodName = configurator.inMethodName;
			inMethodChained = configurator.inMethodChained;
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
		public final Range<Integer> occurances() {
			return occurances;
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
		public Class<?> getPostInputClass() {
			return postInputClass;
		}
	}

	private final SchemaNodeConfigurationContext<? super N> context;

	private Class<?> postInputClass;
	private Range<Integer> occurances;
	private Boolean iterable;
	private String outMethodName;
	private String inMethodName;
	private Boolean inMethodChained;
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
				.getNamespace();
	}

	@Override
	protected DataLoader getDataLoader() {
		return getContext().getDataLoader();
	}

	@Override
	public <V extends T> BindingChildNodeConfigurator<?, ?, V, C, B> dataClass(
			Class<V> dataClass) {
		return (BindingChildNodeConfigurator<?, ?, V, C, B>) super
				.dataClass(dataClass);
	}

	@Override
	public final S occurances(Range<Integer> range) {
		requireConfigurable(occurances);
		occurances = range;
		return getThis();
	}

	@Override
	public final S inMethod(String inMethodName) {
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
	public LinkedHashSet<N> getOverriddenNodes() {
		return getName() == null ? new LinkedHashSet<>() : getContext()
				.overrideChild(getName(), getNodeClass());
	}

	@Override
	protected final boolean isChildContextAbstract() {
		return super.isChildContextAbstract() || getContext().isAbstract();
	}

	@Override
	public S postInputClass(Class<?> postInputClass) {
		requireConfigurable(this.postInputClass);
		this.postInputClass = postInputClass;

		return getThis();
	}
}
