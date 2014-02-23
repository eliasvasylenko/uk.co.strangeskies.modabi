package uk.co.strangeskies.modabi.model.impl;

import java.lang.reflect.Method;
import java.util.List;

import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.model.SequenceNode;
import uk.co.strangeskies.modabi.model.building.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

class SequenceNodeConfiguratorImpl extends
		BranchingNodeConfiguratorImpl<SequenceNodeConfigurator, SequenceNode>
		implements SequenceNodeConfigurator {
	protected static class SequenceNodeImpl extends BranchingNodeImpl implements
			SequenceNode {
		private final String inMethodName;
		private final Method inMethod;
		private final boolean inMethodChained;

		public SequenceNodeImpl(SequenceNodeConfiguratorImpl configurator) {
			super(configurator);

			inMethodName = configurator.inMethod;
			try {
				Class<?> inputClass = configurator.getParent().getInputClass();
				inMethod = inputClass == null ? null : inputClass
						.getMethod(inMethodName);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new SchemaException(e);
			}
			inMethodChained = configurator.inMethodChained;
		}

		public SequenceNodeImpl(SequenceNode node,
				List<SequenceNode> overriddenNodes) {
			super(node, overriddenNodes);

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
		public <U> U process(SchemaProcessingContext<U> context) {
			return context.accept(this);
		}

		@Override
		protected void validateAsEffectiveModel(boolean isAbstract) {
			super.validateAsEffectiveModel(isAbstract);
		}
	}

	private String inMethod;
	private boolean inMethodChained;

	public SequenceNodeConfiguratorImpl(BranchingNodeConfiguratorImpl<?, ?> parent) {
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
	public Class<SequenceNode> getNodeClass() {
		return SequenceNode.class;
	}
}
