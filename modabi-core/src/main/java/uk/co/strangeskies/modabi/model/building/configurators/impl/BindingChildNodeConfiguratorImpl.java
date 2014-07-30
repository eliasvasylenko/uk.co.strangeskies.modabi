package uk.co.strangeskies.modabi.model.building.configurators.impl;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.model.building.configurators.BindingChildNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.ChildNodeImpl;
import uk.co.strangeskies.modabi.model.building.impl.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.SchemaException;

public abstract class BindingChildNodeConfiguratorImpl<S extends BindingChildNodeConfigurator<S, N, T>, N extends BindingChildNode<T, ?>, T, C extends ChildNode<?>, B extends BindingChildNode<?, ?>>
		extends BindingNodeConfiguratorImpl<S, N, T, C, B> implements
		BindingChildNodeConfigurator<S, N, T> {
	protected static abstract class BindingChildNodeImpl<T, E extends BindingChildNode.Effective<T, E>>
			extends BindingNodeImpl<T, E> implements ChildNodeImpl<E>,
			BindingChildNode<T, E> {
		protected static abstract class Effective<T, E extends BindingChildNode.Effective<T, E>>
				extends BindingNodeImpl.Effective<T, E> implements
				BindingChildNode.Effective<T, E> {
			private final Range<Integer> occurances;

			private final Boolean iterable;
			private final String outMethodName;
			private final Method outMethod;

			private final String inMethodName;
			private final Method inMethod;
			private final Boolean inMethodChained;

			protected Effective(
					OverrideMerge<? extends BindingChildNode<?, ?>, ? extends BindingChildNodeConfiguratorImpl<?, ?, ?, ?, ?>> overrideMerge) {
				super(overrideMerge);

				occurances = overrideMerge.getValue(n -> n.occurances(),
						(v, o) -> o.contains(v));

				iterable = overrideMerge.getValue(n -> n.isOutMethodIterable(),
						(n, o) -> Objects.equals(n, o));

				outMethodName = overrideMerge.getValue(n -> n.getOutMethodName());

				inMethodName = overrideMerge.getValue(n -> n.getInMethodName());

				inMethodChained = overrideMerge.getValue(n -> n.isInMethodChained());

				if (outMethodName == "this" && !iterable)
					throw new SchemaException();
				outMethod = (outMethodName == "null") ? null : getOutMethod(
						overrideMerge.configurator().getCurrentChildOutputTargetClass(),
						overrideMerge.getValue(n -> n.effective().getOutMethod(),
								(m, n) -> m.equals(n)));

				inMethod = (inMethodName == "null") ? null : getInMethod(overrideMerge
						.configurator().getCurrentChildInputTargetClass(),
						overrideMerge.getValue(n -> n.effective().getInMethod(),
								(m, n) -> m.equals(n)));
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
			public Method getOutMethod() {
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
			public Method getInMethod() {
				return inMethod;
			}

			@Override
			public final Boolean isInMethodChained() {
				return inMethodChained;
			}

			private Method getInMethod(Class<?> receiverClass,
					Method inheritedInMethod) {
				try {
					return (receiverClass == null || getDataClass() == null || inMethodName == null) ? null
							: BindingNodeConfigurator.findMethod(
									BindingNodeConfigurator.generateInMethodNames(this),
									receiverClass, null, getDataClass());
				} catch (NoSuchMethodException e) {
					throw new SchemaException(e);
				}
			}

			private Method getOutMethod(Class<?> targetClass,
					Method inheritedOutMethod) {
				try {
					Class<?> resultClass = (isOutMethodIterable() != null && isOutMethodIterable()) ? Iterable.class
							: getDataClass();

					Method outMethod;
					if (targetClass == null || resultClass == null
							|| outMethodName == "this")
						outMethod = null;
					else {
						outMethod = BindingNodeConfigurator.findMethod(
								BindingNodeConfigurator.generateOutMethodNames(this,
										resultClass), targetClass, resultClass);

						if (inheritedOutMethod != null
								&& !outMethod.equals(inheritedOutMethod))
							throw new SchemaException();
					}

					return outMethod;
				} catch (NoSuchMethodException e) {
					throw new SchemaException(e);
				}
			}
		}

		private final Range<Integer> occurances;

		private final Boolean iterable;
		private final String outMethodName;

		private final String inMethodName;
		private final Boolean inMethodChained;

		BindingChildNodeImpl(
				BindingChildNodeConfiguratorImpl<?, ?, T, ?, ?> configurator) {
			super(configurator);

			occurances = configurator.occurances;
			iterable = configurator.iterable;
			outMethodName = configurator.outMethodName;

			inMethodName = configurator.inMethodName;
			inMethodChained = configurator.inMethodChained;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof BindingChildNode))
				return false;

			BindingChildNode<?, ?> other = (BindingChildNode<?, ?>) obj;
			return super.equals(obj)
					&& Objects.equals(occurances, other.occurances())
					&& Objects.equals(iterable, other.isOutMethodIterable())
					&& Objects.equals(outMethodName, other.getOutMethodName())
					&& Objects.equals(inMethodName, other.getInMethodName())
					&& Objects.equals(inMethodChained, other.isInMethodChained());
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

	public BindingChildNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super N> parent) {
		this.context = parent;

		addResultListener(result -> parent.addChild(result));
	}

	protected final SchemaNodeConfigurationContext<? super N> getContext() {
		return context;
	}

	@Override
	public <V extends T> BindingChildNodeConfigurator<?, ?, V> dataClass(
			Class<V> dataClass) {
		return (BindingChildNodeConfigurator<?, ?, V>) super.dataClass(dataClass);
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
	protected Set<N> getOverriddenNodes() {
		return (getId() == null || getContext() == null) ? new HashSet<>()
				: getContext().overrideChild(getId(), getNodeClass());
	}
}
