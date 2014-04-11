package uk.co.strangeskies.modabi.model.impl;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.SequenceNode;

public class SequenceNodeConfiguratorImpl extends
		ChildNodeConfiguratorImpl<SequenceNodeConfigurator, SequenceNode> implements
		SequenceNodeConfigurator {
	protected static class SequenceNodeImpl extends SchemaNodeImpl implements
			ChildNodeImpl, SequenceNode {
		private final String inMethodName;
		private final Method inMethod;
		private final boolean inMethodChained;

		public SequenceNodeImpl(SequenceNodeConfiguratorImpl configurator) {
			super(configurator);

			inMethodName = configurator.inMethod;
			try {
				Class<?> inputClass = configurator.getContext()
						.getCurrentChildInputTargetClass();
				inMethod = inputClass == null ? null : inputClass
						.getMethod(inMethodName);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new SchemaException(e);
			}
			inMethodChained = configurator.inMethodChained;

			getPostInputClass();
		}

		public SequenceNodeImpl(SequenceNode node,
				Collection<? extends SequenceNode> overriddenNodes,
				List<ChildNodeImpl> effectiveChildren) {
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

		@Override
		public void unbind(UnbindingChildContext context) {
			for (ChildNodeImpl child : getChildren())
				child.unbind(context);
		}
	}

	private String inMethod;
	private boolean inMethodChained;

	public SequenceNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super SequenceNode> parent) {
		super(parent);
	}

	@Override
	public SequenceNode tryCreate() {
		return new SequenceNodeImpl(this);
	}

	@Override
	public SequenceNodeConfigurator inMethod(String methodName) {
		inMethod = methodName;

		return this;
	}

	@Override
	public SequenceNodeConfigurator inMethodChained(boolean chained) {
		inMethodChained = chained;

		return this;
	}

	@Override
	protected Class<SequenceNode> getNodeClass() {
		return SequenceNode.class;
	}

	@Override
	protected Class<?> getCurrentChildInputTargetClass() {
		if (getChildren().isEmpty())
			return getContext().getCurrentChildInputTargetClass();
		else
			return getChildren().get(getChildren().size() - 1).getPostInputClass();
	}

	@Override
	protected SequenceNode getEffective(SequenceNode node) {
		return new SequenceNodeImpl(node, getOverriddenNodes(),
				getEffectiveChildren());
	}

	@Override
	public ChildBuilder addChild() {
		return super.addChild();
	}
}
