package uk.co.strangeskies.modabi.model.building.configurators.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.model.building.configurators.BindingChildNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.ChildNodeImpl;
import uk.co.strangeskies.modabi.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.model.building.impl.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.SchemaException;

public abstract class BindingChildNodeConfiguratorImpl<S extends BindingChildNodeConfigurator<S, N, T>, N extends BindingChildNode<T>, T, C extends ChildNode, B extends BindingChildNode<?>>
		extends BindingNodeConfiguratorImpl<S, N, T, C, B> implements
		BindingChildNodeConfigurator<S, N, T> {
	protected static abstract class BindingChildNodeImpl<T> extends
			BindingNodeImpl<T> implements ChildNodeImpl, BindingChildNode<T> {
		private final Range<Integer> occurances;

		private final Boolean iterable;
		private final String outMethodName;
		private final Method outMethod;

		private final String inMethodName;
		private final Method inMethod;
		private final Boolean inMethodChained;

		BindingChildNodeImpl(
				BindingChildNodeConfiguratorImpl<?, ?, T, ?, ?> configurator) {
			super(configurator);

			occurances = configurator.occurances;
			iterable = configurator.iterable;
			outMethodName = configurator.outMethodName;
			outMethod = null;

			inMethodName = configurator.inMethodName;
			inMethod = null;
			inMethodChained = configurator.inMethodChained;
		}

		BindingChildNodeImpl(BindingChildNode<T> node,
				Collection<? extends BindingChildNode<? super T>> overriddenNodes,
				List<ChildNode> effectiveChildren, Class<?> outputTargetClass,
				Class<?> inputTargetClass) {
			super(node, overriddenNodes, effectiveChildren);

			OverrideMerge<BindingChildNode<? super T>> overrideMerge = new OverrideMerge<>(
					node, overriddenNodes);

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
					outputTargetClass,
					overrideMerge.getValue(n -> n.getOutMethod(), (m, n) -> m.equals(n)));

			inMethod = (inMethodName == "null") ? null : getInMethod(
					inputTargetClass,
					overrideMerge.getValue(n -> n.getInMethod(), (m, n) -> m.equals(n)));

		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof BindingChildNode))
				return false;

			BindingChildNode<?> other = (BindingChildNode<?>) obj;
			return super.equals(obj)
					&& Objects.equals(occurances, other.occurances())
					&& Objects.equals(iterable, other.isOutMethodIterable())
					&& Objects.equals(outMethodName, other.getOutMethodName())
					&& Objects.equals(outMethod, other.getOutMethod())
					&& Objects.equals(inMethodName, other.getInMethodName())
					&& Objects.equals(inMethod, other.getInMethod())
					&& Objects.equals(inMethodChained, other.isInMethodChained());
		}

		private Method getInMethod(Class<?> receiverClass, Method inheritedInMethod) {
			try {
				return (receiverClass == null || getDataClass() == null || inMethodName == null) ? null
						: BindingNodeConfigurator.findMethod(
								BindingNodeConfigurator.generateInMethodNames(this),
								receiverClass, null, getDataClass());
			} catch (NoSuchMethodException e) {
				throw new SchemaException(e);
			}
		}

		private Method getOutMethod(Class<?> receiverClass,
				Method inheritedOutMethod) {
			try {
				Class<?> resultClass = (isOutMethodIterable() != null && isOutMethodIterable()) ? Iterable.class
						: getDataClass();

				return (receiverClass == null || resultClass == null || outMethodName == "this") ? null
						: inheritedOutMethod != null ? inheritedOutMethod
								: BindingNodeConfigurator.findMethod(BindingNodeConfigurator
										.generateOutMethodNames(this, resultClass), receiverClass,
										resultClass);
			} catch (NoSuchMethodException e) {
				throw new SchemaException(e);
			}
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
		public final Method getOutMethod() {
			return outMethod;
		}

		@Override
		public final Method getInMethod() {
			return inMethod;
		}

		@SuppressWarnings("unchecked")
		protected Iterable<T> getData(Object parent) {
			try {
				if (isOutMethodIterable() != null && isOutMethodIterable()) {
					if (getOutMethodName() == "this")
						return (Iterable<T>) parent;
					else
						return (Iterable<T>) getOutMethod().invoke(parent);
				} else
					return Arrays.asList((T) getOutMethod().invoke(parent));
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new SchemaException(e);
			}
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

		addResultListener(result -> parent.addChild(result, getEffective(result)));
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

	protected final Set<N> getOverriddenNodes() {
		return (getId() == null || getContext() == null) ? new HashSet<>()
				: getContext().overrideChild(getId(), getNodeClass());
	}
}
