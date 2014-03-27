package uk.co.strangeskies.modabi.model.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.set.ListOrderedSet;

import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.building.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.BindingNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;
import uk.co.strangeskies.modabi.processing.SchemaResultProcessingContext;
import uk.co.strangeskies.modabi.processing.UnbindingStrategy;

public abstract class BindingNodeConfiguratorImpl<S extends BindingNodeConfigurator<S, N, T>, N extends BindingNode<T>, T>
		extends SchemaNodeConfiguratorImpl<S, N> implements
		BindingNodeConfigurator<S, N, T> {
	protected abstract static class BindingNodeImpl<T> extends SchemaNodeImpl
			implements BindingNode<T> {
		private final Class<T> dataClass;
		private final Class<?> bindingClass;
		private final Class<?> unbindingClass;
		private final BindingStrategy bindingStrategy;
		private final UnbindingStrategy unbindingStrategy;
		private final Method unbindingMethod;

		public BindingNodeImpl(BindingNodeConfiguratorImpl<?, ?, T> configurator) {
			super(configurator);

			dataClass = configurator.dataClass;

			bindingStrategy = configurator.bindingStrategy;
			bindingClass = configurator.bindingClass;

			unbindingStrategy = configurator.unbindingStrategy;
			unbindingClass = configurator.unbindingClass;
			unbindingMethod = null; // TODO
		}

		@SuppressWarnings("unchecked")
		public BindingNodeImpl(BindingNode<? super T> node,
				Collection<? extends BindingNode<? super T>> overriddenNodes,
				List<ChildNode> effectiveChildren) {
			super(node, overriddenNodes, effectiveChildren);

			dataClass = (Class<T>) getValue(node, overriddenNodes,
					n -> n.getDataClass(), (v, o) -> o.isAssignableFrom(v));

			bindingClass = getValue(node, overriddenNodes, n -> n.getBindingClass());

			unbindingClass = getValue(node, overriddenNodes,
					n -> n.getUnbindingClass());

			bindingStrategy = getValue(node, overriddenNodes,
					n -> n.getBindingStrategy());

			unbindingStrategy = getValue(node, overriddenNodes,
					n -> n.getUnbindingStrategy());

			unbindingMethod = getValue(node, overriddenNodes,
					n -> n.getUnbindingMethod());
		}

		protected static <T> Collection<AbstractModel<? super T>> overriddenWithBase(
				AbstractModel<? super T> node,
				Collection<? extends AbstractModel<? super T>> overriddenNodes) {
			List<AbstractModel<? super T>> overriddenAndModelNodes = new ArrayList<>();
			overriddenAndModelNodes.addAll(overriddenNodes);

			ListOrderedSet<Model<? super T>> baseModels = new ListOrderedSet<>();
			baseModels.addAll(node.getBaseModel());
			/*
			 * for (int i = 0; i < baseModels.size(); i++) { Model<? super T>
			 * baseModel = baseModels.get(i);
			 * baseModels.addAll(baseModel.effectiveModel().getBaseModel()); }
			 */
			overriddenAndModelNodes.addAll(baseModels);

			return overriddenAndModelNodes;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof AbstractModel))
				return false;
			return super.equals(obj);
		}

		@Override
		public final Class<T> getDataClass() {
			return dataClass;
		}

		@Override
		public final BindingStrategy getBindingStrategy() {
			return bindingStrategy;
		}

		@Override
		public final UnbindingStrategy getUnbindingStrategy() {
			return unbindingStrategy;
		}

		@Override
		public final Class<?> getBindingClass() {
			return bindingClass;
		}

		@Override
		public final Class<?> getUnbindingClass() {
			return unbindingClass;
		}

		@Override
		public void process(SchemaProcessingContext context) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <U> U process(SchemaResultProcessingContext<U> context) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Method getUnbindingMethod() {
			return unbindingMethod;
		}
	}

	private Class<T> dataClass;

	private BindingStrategy bindingStrategy;
	private Class<?> bindingClass;

	private UnbindingStrategy unbindingStrategy;
	private Class<?> unbindingClass;
	public String unbindingMethod;

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> BindingNodeConfigurator<?, ?, V> dataClass(
			Class<V> dataClass) {
		requireConfigurable(this.dataClass);
		this.dataClass = (Class<T>) dataClass;

		return (BindingNodeConfigurator<?, ?, V>) this;
	}

	@Override
	protected final Class<T> getCurrentChildOutputTargetClass() {
		return dataClass;
	}

	@Override
	protected Class<?> getCurrentChildInputTargetClass() {
		if (getChildren().isEmpty())
			return bindingClass != null ? bindingClass : dataClass;
		else
			return getChildren().get(getChildren().size() - 1).getPostInputClass();
	}

	@Override
	public final S bindingClass(Class<?> bindingClass) {
		requireConfigurable(this.bindingClass);
		this.bindingClass = bindingClass;

		return getThis();
	}

	@Override
	public S unbindingClass(Class<?> unbindingClass) {
		requireConfigurable(this.unbindingClass);
		this.unbindingClass = unbindingClass;

		return getThis();
	}

	@Override
	public S unbindingMethod(String unbindingMethod) {
		requireConfigurable(this.unbindingMethod);
		this.unbindingMethod = unbindingMethod;

		return getThis();
	}

	protected final Class<?> getBuilderClass() {
		return bindingClass;
	}

	@Override
	public final S bindingStrategy(BindingStrategy strategy) {
		requireConfigurable(bindingStrategy);
		bindingStrategy = strategy;

		return getThis();
	}

	@Override
	public final S unbindingStrategy(UnbindingStrategy strategy) {
		requireConfigurable(unbindingStrategy);
		unbindingStrategy = strategy;

		return getThis();
	}
}
