package uk.co.strangeskies.modabi.model.building.impl;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.InputSequenceNode;

public class InputSequenceNodeConfiguratorImpl<C extends BindingChildNode<?>>
		extends
		ChildNodeConfiguratorImpl<InputSequenceNodeConfigurator<C>, InputSequenceNode, C, C>
		implements InputSequenceNodeConfigurator<C> {
	protected static class InputSequenceNodeImpl extends SchemaNodeImpl implements
			ChildNodeImpl, InputSequenceNode {
		private final String inMethodName;
		private final Method inMethod;
		private final Boolean inMethodChained;

		public InputSequenceNodeImpl(
				InputSequenceNodeConfiguratorImpl<?> configurator) {
			super(configurator);

			inMethodName = configurator.inMethodName;
			Method inMethod = null;
			try {
				Class<?> inputClass = configurator.getContext()
						.getCurrentChildInputTargetClass();

				List<Class<?>> parameterClasses = configurator.getChildren() == null ? null
						: configurator.getChildren().stream()
								.map(o -> ((BindingChildNode<?>) o).getDataClass())
								.collect(Collectors.toList());

				inMethod = (inputClass == null || parameterClasses == null
						|| parameterClasses.stream().anyMatch(Objects::isNull) || inMethodName == null) ? null
						: inputClass.getMethod(inMethodName,
								parameterClasses.toArray(new Class[0]));
			} catch (NoSuchMethodException | SecurityException e) {
			}
			this.inMethod = inMethod;
			inMethodChained = configurator.inMethodChained;

			getPostInputClass();
		}

		public InputSequenceNodeImpl(InputSequenceNode node,
				Collection<? extends InputSequenceNode> overriddenNodes,
				List<ChildNode> effectiveChildren) {
			super(node, overriddenNodes, effectiveChildren);

			OverrideMerge<InputSequenceNode> overrideMerge = new OverrideMerge<>(
					node, overriddenNodes);

			inMethodName = overrideMerge.getValue(n -> n.getInMethodName(),
					(m, n) -> m.equals(n));

			inMethod = overrideMerge.getValue(n -> n.getInMethod(),
					(m, n) -> m.equals(n));

			inMethodChained = overrideMerge.getValue(n -> n.isInMethodChained());
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof InputSequenceNode))
				return false;

			InputSequenceNode other = (InputSequenceNode) obj;
			return super.equals(obj)
					&& Objects.equals(inMethodName, other.getInMethodName())
					&& Objects.equals(inMethod, other.getInMethod())
					&& Objects.equals(inMethodChained, other.isInMethodChained());
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
	}

	private String inMethodName;
	private Boolean inMethodChained;

	public InputSequenceNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super InputSequenceNode> parent) {
		super(parent);
	}

	@Override
	public InputSequenceNode tryCreate() {
		return new InputSequenceNodeImpl(this);
	}

	@Override
	public InputSequenceNodeConfigurator<C> inMethod(String methodName) {
		inMethodName = methodName;

		return this;
	}

	@Override
	public InputSequenceNodeConfigurator<C> inMethodChained(boolean chained) {
		inMethodChained = chained;

		return this;
	}

	@Override
	protected Class<InputSequenceNode> getNodeClass() {
		return InputSequenceNode.class;
	}

	@Override
	protected Class<?> getCurrentChildInputTargetClass() {
		if (getChildren().isEmpty())
			return getContext().getCurrentChildInputTargetClass();
		else
			return getChildren().get(getChildren().size() - 1).getPostInputClass();
	}

	@Override
	protected InputSequenceNode getEffective(InputSequenceNode node) {
		return new InputSequenceNodeImpl(node, getOverriddenNodes(),
				getEffectiveChildren());
	}

	@Override
	public ChildBuilder<C, C> addChild() {
		return childBuilder();
	}
}
