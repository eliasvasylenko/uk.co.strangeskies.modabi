package uk.co.strangeskies.modabi.model.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.model.building.BindingChildNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;

public abstract class BindingChildNodeConfiguratorImpl<S extends BindingChildNodeConfigurator<S, N, T>, N extends BindingChildNode<T>, T>
		extends BindingNodeConfiguratorImpl<S, N, T> implements
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

		public BindingChildNodeImpl(
				BindingChildNodeConfiguratorImpl<?, ?, T> configurator) {
			super(configurator);

			occurances = configurator.occurances;
			iterable = configurator.iterable;
			outMethodName = configurator.outMethodName;
			if (outMethodName == "this" && !iterable)
				throw new SchemaException();
			inMethodName = configurator.inMethodName;

			Method outMethod = null;
			try {
				Class<?> outputClass = configurator.getContext()
						.getCurrentChildOutputTargetClass();
				Class<?> resultClass = (isOutMethodIterable() == null || isOutMethodIterable()) ? resultClass = Iterable.class
						: getDataClass();
				outMethod = (getId() == null || outputClass == null
						|| resultClass == null || outMethodName == "this") ? null
						: BindingNodeConfigurator.findMethod(BindingNodeConfigurator
								.generateOutMethodNames(this, resultClass), outputClass,
								resultClass);

			} catch (NoSuchMethodException | SecurityException e) {
			}
			this.outMethod = outMethod;

			Method inMethod = null;
			try {
				Class<?> inputClass = configurator.getContext()
						.getCurrentChildInputTargetClass();
				inMethod = (inputClass == null || getDataClass() == null || inMethodName == null) ? null
						: inputClass.getMethod(inMethodName, getDataClass());
			} catch (NoSuchMethodException | SecurityException e) {
			}
			this.inMethod = inMethod;

			inMethodChained = configurator.inMethodChained;
		}

		BindingChildNodeImpl(BindingChildNode<T> node,
				Collection<? extends BindingChildNode<? super T>> overriddenNodes,
				List<ChildNode> effectiveChildren, Class<?> parentClass) {
			super(node, overriddenNodes, effectiveChildren);

			occurances = getValue(node, overriddenNodes, n -> n.occurances(),
					(v, o) -> o.contains(v));

			iterable = getValue(node, overriddenNodes, n -> n.isOutMethodIterable(),
					(n, o) -> Objects.equals(n, o));

			outMethodName = getValue(node, overriddenNodes, n -> n.getOutMethodName());

			Class<?> resultClass = (isOutMethodIterable() != null && isOutMethodIterable()) ? resultClass = Iterable.class
					: getDataClass();
			Method inheritedOutMethod = getValue(node, overriddenNodes,
					n -> n.getOutMethod());
			try {
				outMethod = outMethodName == "this" ? null
						: inheritedOutMethod != null ? inheritedOutMethod
								: BindingNodeConfigurator.findMethod(
										BindingNodeConfigurator.generateOutMethodNames(this),
										parentClass, resultClass);
			} catch (NoSuchMethodException e) {
				throw new SchemaException(e);
			}

			inMethodName = getValue(node, overriddenNodes, n -> n.getInMethodName());

			inMethod = getValue(node, overriddenNodes, n -> n.getInMethod(),
					(m, n) -> m.equals(n));

			inMethodChained = getValue(node, overriddenNodes,
					n -> n.isInMethodChained());
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

		@Override
		public final Class<?> getPreInputClass() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public final Class<?> getPostInputClass() {
			// TODO Auto-generated method stub
			return null;
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

	protected final List<N> getOverriddenNodes() {
		return (getId() == null || getContext() == null) ? new ArrayList<>()
				: getContext().overrideChild(getId(), getNodeClass());
	}
}
