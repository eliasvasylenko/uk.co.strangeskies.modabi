package uk.co.strangeskies.modabi.model.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.data.BufferedDataTarget;
import uk.co.strangeskies.modabi.data.TerminatingDataTarget;
import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.EffectiveModel;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.ModelConfigurator;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.processing.UnbindingContext;

public class ModelConfiguratorImpl<T> extends
		BindingNodeConfiguratorImpl<ModelConfigurator<T>, Model<T>, T> implements
		ModelConfigurator<T> {
	protected static abstract class AbstractModelImpl<T> extends
			BindingNodeImpl<T> implements AbstractModel<T> {
		private final List<Model<? super T>> baseModel;
		private final Boolean isAbstract;

		public AbstractModelImpl(ModelConfiguratorImpl<T> configurator) {
			super(configurator);

			baseModel = configurator.baseModel == null ? new ArrayList<>()
					: new ArrayList<>(configurator.baseModel);
			isAbstract = configurator.isAbstract;
		}

		public AbstractModelImpl(AbstractModel<T> node,
				Collection<? extends AbstractModel<? super T>> overriddenNodes,
				List<ChildNode> effectiveChildren) {
			this(node, overriddenWithBase(node, overriddenNodes), effectiveChildren,
					null);
		}

		private AbstractModelImpl(AbstractModel<T> node,
				Collection<AbstractModel<? super T>> overriddenNodes,
				List<ChildNode> effectiveChildren, Void flag) {
			super(node, overriddenNodes, effectiveChildren);

			baseModel = new ArrayList<>();
			overriddenNodes.forEach(n -> baseModel.addAll(n.getBaseModel()));
			baseModel.addAll(node.getBaseModel());

			isAbstract = getValue(node, overriddenNodes, n -> n.isAbstract());
		}

		private static <T> Collection<AbstractModel<? super T>> overriddenWithBase(
				AbstractModel<? super T> node,
				Collection<? extends AbstractModel<? super T>> overriddenNodes) {
			List<AbstractModel<? super T>> overriddenAndModelNodes = new ArrayList<>();

			overriddenAndModelNodes.addAll(overriddenNodes);
			overriddenAndModelNodes.addAll(node.getBaseModel());

			return overriddenAndModelNodes;
		}

		@Override
		public final Boolean isAbstract() {
			return isAbstract;
		}

		@Override
		public final List<Model<? super T>> getBaseModel() {
			return baseModel;
		}
	}

	protected static class EffectiveModelImpl<T> extends AbstractModelImpl<T>
			implements EffectiveModel<T> {
		public EffectiveModelImpl(ModelImpl<T> node,
				Collection<? extends EffectiveModel<? super T>> overriddenNodes,
				List<ChildNode> effectiveChildren) {
			super(node, overriddenNodes, effectiveChildren);
		}

		@Override
		protected void unbind(UnbindingChildContext context) {
			throw new AssertionError();
		}
	}

	protected static class ModelImpl<T> extends AbstractModelImpl<T> implements
			Model<T> {
		private final EffectiveModel<T> effectiveModel;

		public ModelImpl(ModelConfiguratorImpl<T> configurator) {
			super(configurator);

			effectiveModel = new EffectiveModelImpl<T>(this, getBaseModel().stream()
					.map(m -> m.effectiveModel()).collect(Collectors.toList()),
					configurator.getEffectiveChildren());
		}

		@Override
		public EffectiveModel<T> effectiveModel() {
			return effectiveModel;
		}

		@Override
		protected void unbind(UnbindingChildContext context) {
			throw new AssertionError();
		}

		@Override
		public void unbind(UnbindingContext<T> context) {
			List<BufferedDataTarget> bufferedSimpleElements = new ArrayList<>();

			UnbindingChildContext childContext = new UnbindingChildContext() {
				private final Deque<Object> bindingStack = new ArrayDeque<>();
				private TerminatingDataTarget sink = null;

				@Override
				public TerminatingDataTarget simpleElement(String id) {
					BufferedDataTarget buffer = new BufferedDataTarget();
					bufferedSimpleElements.add(buffer);
					return sink = buffer;
				}

				@Override
				public TerminatingDataTarget property(String id) {
					return sink = context.output().property(id);
				}

				@Override
				public Object getUnbindingTarget() {
					return bindingStack.peek();
				}

				@Override
				public void pushTarget(Object target) {
					bindingStack.push(target);
				}

				@Override
				public void popTarget() {
					bindingStack.pop();
				}

				@Override
				public TerminatingDataTarget getOpenDataTarget() {
					return sink;
				}

				@Override
				public void endData() {
					sink.end();
					sink = null;
				}

				@Override
				public TerminatingDataTarget content() {
					return sink = context.output().content();
				}

				@Override
				public <U> List<Model<? extends U>> getMatchingModels(
						AbstractModel<U> element, Class<?> dataClass) {
					return context.getMatchingModels(element, dataClass);
				}
			};

			childContext.pushTarget(context.data());

			for (ChildNode child : getChildren())
				((SchemaNodeImpl) child).unbind(childContext);

			for (BufferedDataTarget bufferedSimpleElement : bufferedSimpleElements) {
			}
		}
	}

	private List<Model<? super T>> baseModel;
	private Boolean isAbstract;

	@Override
	public final ModelConfigurator<T> isAbstract(boolean isAbstract) {
		requireConfigurable(this.isAbstract);
		this.isAbstract = isAbstract;

		return getThis();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ModelConfigurator<V> baseModel(Model<? super V>... base) {
		requireConfigurable(this.baseModel);
		ModelConfiguratorImpl<V> thisV = (ModelConfiguratorImpl<V>) this;
		thisV.baseModel = Arrays.asList(base);

		baseModel.forEach(m -> {
			inheritChildren(m.effectiveModel().getChildren());
		});

		return thisV;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ModelConfigurator<V> dataClass(Class<V> dataClass) {
		return (ModelConfigurator<V>) super.dataClass(dataClass);
	}

	@Override
	public Model<T> tryCreate() {
		return new ModelImpl<>(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Class<Model<T>> getNodeClass() {
		return (Class<Model<T>>) (Object) Model.class;
	}

	@Override
	protected Model<T> getEffective(Model<T> node) {
		return null;
	}

	@Override
	public ChildBuilder addChild() {
		return super.addChild();
	}
}
