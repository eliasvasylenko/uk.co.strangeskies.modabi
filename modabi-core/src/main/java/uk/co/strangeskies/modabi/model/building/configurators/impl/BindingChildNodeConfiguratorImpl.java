package uk.co.strangeskies.modabi.model.building.configurators.impl;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Objects;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.model.building.DataLoader;
import uk.co.strangeskies.modabi.model.building.configurators.BindingChildNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.ChildNodeImpl;
import uk.co.strangeskies.modabi.model.building.impl.Methods;
import uk.co.strangeskies.modabi.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.model.building.impl.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.schema.SchemaException;

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

			private Boolean isExtensible;

			protected Effective(
					OverrideMerge<S, ? extends BindingChildNodeConfiguratorImpl<?, ?, ?, ?, ?>> overrideMerge) {
				super(overrideMerge);

				isExtensible = overrideMerge.getValue(BindingChildNode::isExtensible);

				if (isAbstract()
						&& !overrideMerge.configurator().getContext().isAbstract()
						&& !(isExtensible() != null && isExtensible()))
					throw new SchemaException(
							"Node '"
									+ getName()
									+ "' is not extensible and has no abstract parents, so cannot be abstract.");

				occurances = overrideMerge.getValue(BindingChildNode::occurances,
						(v, o) -> o.contains(v));

				iterable = overrideMerge.getValue(
						BindingChildNode::isOutMethodIterable, Objects::equals);

				outMethodName = overrideMerge
						.getValue(BindingChildNode::getOutMethodName);

				inMethodName = overrideMerge
						.getValue(BindingChildNode::getInMethodName);

				inMethodChained = overrideMerge
						.getValue(BindingChildNode::isInMethodChained);

				outMethod = (outMethodName == "null") ? null : Methods.getOutMethod(
						this, overrideMerge.configurator().getContext()
								.getOutputTargetClass(), overrideMerge.getValue(n -> n
								.effective() == null ? null : n.effective().getOutMethod(),
								Objects::equals));

				inMethod = (inMethodName == "null") ? null : Methods.getInMethod(this,
						overrideMerge.configurator().getContext().getInputTargetClass(),
						overrideMerge.getValue(n -> n.effective() == null ? null : n
								.effective().getInMethod(), Objects::equals));
			}

			@Override
			public final Boolean isExtensible() {
				return isExtensible;
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

		private final Range<Integer> occurances;

		private final Boolean iterable;
		private final String outMethodName;

		private final String inMethodName;
		private final Boolean inMethodChained;

		private Boolean isExtensible;

		BindingChildNodeImpl(
				BindingChildNodeConfiguratorImpl<?, ?, T, ?, ?> configurator) {
			super(configurator);

			isExtensible = configurator.isExtensible;

			occurances = configurator.occurances;
			iterable = configurator.iterable;
			outMethodName = configurator.outMethodName;

			inMethodName = configurator.inMethodName;
			inMethodChained = configurator.inMethodChained;
		}

		@Override
		public final Boolean isExtensible() {
			return isExtensible;
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
	}

	private final SchemaNodeConfigurationContext<? super N> context;

	private Range<Integer> occurances;
	private Boolean iterable;
	private String outMethodName;
	private String inMethodName;
	private Boolean inMethodChained;
	private Boolean isExtensible;

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
	public final S isExtensible(boolean isExtensible) {
		requireConfigurable(this.isExtensible);
		this.isExtensible = isExtensible;

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
}
