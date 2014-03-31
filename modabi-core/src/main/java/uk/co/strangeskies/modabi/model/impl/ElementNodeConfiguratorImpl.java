package uk.co.strangeskies.modabi.model.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;
import uk.co.strangeskies.modabi.processing.SchemaResultProcessingContext;
import uk.co.strangeskies.modabi.processing.UnbindingStrategy;

public class ElementNodeConfiguratorImpl<T>
		extends
		BindingChildNodeConfiguratorImpl<ElementNodeConfigurator<T>, ElementNode<T>, T>
		implements ElementNodeConfigurator<T> {
	protected static class ElementNodeImpl<T> extends BindingChildNodeImpl<T>
			implements ElementNode<T> {
		private final List<Model<? super T>> baseModel;
		private final Boolean isAbstract;

		public ElementNodeImpl(ElementNodeConfiguratorImpl<T> configurator) {
			super(configurator);

			baseModel = configurator.baseModel == null ? new ArrayList<>()
					: new ArrayList<>(configurator.baseModel);
			isAbstract = configurator.isAbstract;
		}

		public ElementNodeImpl(ElementNode<T> node,
				Collection<? extends ElementNode<? super T>> overriddenNodes,
				List<ChildNode> effectiveChildren, Class<?> parentClass) {
			this(node, overriddenWithBase(node, overriddenNodes), effectiveChildren,
					parentClass, null);
		}

		private ElementNodeImpl(ElementNode<T> node,
				Collection<ElementNode<? super T>> overriddenNodes,
				List<ChildNode> effectiveChildren, Class<?> parentClass, Void flag) {
			super(node, overriddenNodes, effectiveChildren, parentClass);

			baseModel = new ArrayList<>();
			overriddenNodes.forEach(n -> baseModel.addAll(n.getBaseModel()));
			baseModel.addAll(node.getBaseModel());

			isAbstract = getValue(node, overriddenNodes, n -> n.isAbstract());
		}

		protected static <T> Collection<ElementNode<? super T>> overriddenWithBase(
				ElementNode<? super T> node,
				Collection<? extends ElementNode<? super T>> overriddenNodes) {
			List<ElementNode<? super T>> overriddenAndModelNodes = new ArrayList<>();

			overriddenAndModelNodes.addAll(overriddenNodes);
			for (Model<? super T> base : node.getBaseModel())
				overriddenAndModelNodes.add(new ElementNode<Object>() {
					@Override
					public Boolean isAbstract() {
						return base.isAbstract();
					}

					@SuppressWarnings("unchecked")
					@Override
					public List<Model<? super Object>> getBaseModel() {
						return (List<Model<? super Object>>) (Object) base.getBaseModel();
					}

					@SuppressWarnings("unchecked")
					@Override
					public Class<Object> getDataClass() {
						return (Class<Object>) base.getDataClass();
					}

					@Override
					public BindingStrategy getBindingStrategy() {
						return base.getBindingStrategy();
					}

					@Override
					public Class<?> getBindingClass() {
						return base.getBindingClass();
					}

					@Override
					public UnbindingStrategy getUnbindingStrategy() {
						return base.getUnbindingStrategy();
					}

					@Override
					public Class<?> getUnbindingClass() {
						return base.getUnbindingClass();
					}

					@Override
					public String getUnbindingMethodName() {
						return base.getUnbindingMethodName();
					}

					@Override
					public Method getUnbindingMethod() {
						return base.getUnbindingMethod();
					}

					@Override
					public String getId() {
						return base.getId();
					}

					@Override
					public void process(SchemaProcessingContext context) {
						base.process(context);
					}

					@Override
					public <U> U process(SchemaResultProcessingContext<U> context) {
						return base.process(context);
					}

					@Override
					public List<? extends ChildNode> getChildren() {
						return base.getChildren();
					}

					@Override
					public Method getOutMethod() {
						return null;
					}

					@Override
					public String getOutMethodName() {
						return null;
					}

					@Override
					public Boolean isOutMethodIterable() {
						return null;
					}

					@Override
					public Range<Integer> occurances() {
						return null;
					}

					@Override
					public String getInMethodName() {
						return null;
					}

					@Override
					public Method getInMethod() {
						return null;
					}

					@Override
					public Boolean isInMethodChained() {
						return null;
					}

					@Override
					public Class<?> getPreInputClass() {
						return null;
					}

					@Override
					public Class<?> getPostInputClass() {
						return null;
					}
				});

			return overriddenAndModelNodes;
		}

		@Override
		public Boolean isAbstract() {
			return isAbstract;
		}

		@Override
		public List<Model<? super T>> getBaseModel() {
			return baseModel;
		}

		@Override
		public void process(SchemaProcessingContext context) {
			context.accept(this);
		}

		@Override
		public <U> U process(SchemaResultProcessingContext<U> context) {
			return context.accept(this);
		}
	}

	private List<Model<? super T>> baseModel;
	private Boolean isAbstract;

	public ElementNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super ElementNode<T>> parent) {
		super(parent);
	}

	@Override
	public final ElementNodeConfigurator<T> isAbstract(boolean isAbstract) {
		requireConfigurable(this.isAbstract);
		this.isAbstract = isAbstract;

		return getThis();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ElementNodeConfigurator<V> baseModel(
			Model<? super V>... base) {
		requireConfigurable(this.baseModel);
		ElementNodeConfiguratorImpl<V> thisV = (ElementNodeConfiguratorImpl<V>) this;
		thisV.baseModel = Arrays.asList(base);

		baseModel.forEach(m -> {
			inheritChildren(m.effectiveModel().getChildren());
		});

		return thisV;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ElementNodeConfigurator<V> dataClass(Class<V> dataClass) {
		return (ElementNodeConfigurator<V>) super.dataClass(dataClass);
	}

	@Override
	protected ElementNode<T> getEffective(ElementNode<T> node) {
		return new ElementNodeImpl<T>(node, getOverriddenNodes(),
				getEffectiveChildren(), getContext().getCurrentChildOutputTargetClass());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Class<ElementNode<T>> getNodeClass() {
		return (Class<ElementNode<T>>) (Object) ElementNode.class;
	}

	@Override
	protected ElementNode<T> tryCreate() {
		return new ElementNodeImpl<>(this);
	}

	@Override
	public ChildBuilder addChild() {
		return super.addChild();
	}
}
