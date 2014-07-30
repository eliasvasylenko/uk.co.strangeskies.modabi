package uk.co.strangeskies.modabi.model.nodes;

import java.lang.reflect.Method;

import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface InputSequenceNode extends
		InputNode<InputSequenceNode.Effective>,
		DataNodeChildNode<InputSequenceNode.Effective> {
	interface Effective extends InputSequenceNode, InputNode.Effective<Effective> {
		@Override
		default Class<?> getPreInputClass() {
			Method inMethod = getInMethod();
			return inMethod == null ? null : inMethod.getDeclaringClass();
		}

		@Override
		default Class<?> getPostInputClass() {
			Method inMethod = getInMethod();
			return inMethod == null ? null : inMethod.getReturnType();
		}

		@Override
		default void process(SchemaProcessingContext context) {
			context.accept(this);
		}
	}
}
