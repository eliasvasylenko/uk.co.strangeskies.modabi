package uk.co.strangeskies.modabi.model.impl;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.InputSequenceNode;

public class InputSequenceNodeConfiguratorImpl extends
		ChildNodeConfiguratorImpl<InputSequenceNodeConfigurator, InputSequenceNode>
		implements InputSequenceNodeConfigurator {
	protected static class InputSequenceNodeImpl extends SchemaNodeImpl implements
			ChildNodeImpl, InputSequenceNode {
		private final String inMethodName;
		private final Method inMethod;
		private final boolean inMethodChained;

		public InputSequenceNodeImpl(InputSequenceNodeConfiguratorImpl configurator) {
			super(configurator);

			inMethodName = configurator.inMethodName;
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

			getPostInputClass();
		}

		public InputSequenceNodeImpl(InputSequenceNode node,
				Collection<? extends InputSequenceNode> overriddenNodes,
				List<ChildNode> effectiveChildren) {
			super(node, overriddenNodes, effectiveChildren);

			inMethodName = getValue(node, overriddenNodes, n -> n.getInMethodName(),
					(m, n) -> m.equals(n));

			inMethod = getValue(node, overriddenNodes, n -> n.getInMethod(),
					(m, n) -> m.equals(n));

			inMethodChained = getValue(node, overriddenNodes,
					n -> n.isInMethodChained());
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
	private boolean inMethodChained;

	public InputSequenceNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super InputSequenceNode> parent) {
		super(parent);
	}

	@Override
	public InputSequenceNode tryCreate() {
		return new InputSequenceNodeImpl(this);
	}

	@Override
	public InputSequenceNodeConfigurator inMethod(String methodName) {
		inMethodName = methodName;

		return this;
	}

	@Override
	public InputSequenceNodeConfigurator inMethodChained(boolean chained) {
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
	public ChildBuilder addChild() {
		return childBuilder();
	}
}
