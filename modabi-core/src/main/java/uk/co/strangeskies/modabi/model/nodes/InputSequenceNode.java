package uk.co.strangeskies.modabi.model.nodes;

import java.lang.reflect.Method;

import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface InputSequenceNode extends
		InputNode<InputSequenceNode, InputSequenceNode.Effective>,
		DataNodeChildNode<InputSequenceNode, InputSequenceNode.Effective> {
	interface Effective extends InputSequenceNode,
			InputNode.Effective<InputSequenceNode, Effective> {
		@Override
		default Class<?> getPreInputClass() {
			Method inMethod = getInMethod();
			return inMethod == null ? null : inMethod.getDeclaringClass();
		}

		@Override
		default Class<?> getPostInputClass() {
			if (isInMethodChained() == null || !isInMethodChained())
				return getPreInputClass();
			Method inMethod = getInMethod();
			return inMethod == null ? null : inMethod.getReturnType();
		}

		@Override
		default void process(SchemaProcessingContext context) {
			context.accept(this);
		}
	}

	@Override
	default Class<Effective> getEffectiveClass() {
		return Effective.class;
	}

	@Override
	default Class<InputSequenceNode> getNodeClass() {
		return InputSequenceNode.class;
	}
}
