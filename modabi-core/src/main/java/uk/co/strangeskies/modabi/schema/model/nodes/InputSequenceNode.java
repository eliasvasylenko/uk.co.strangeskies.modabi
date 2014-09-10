package uk.co.strangeskies.modabi.schema.model.nodes;

import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface InputSequenceNode extends
		InputNode<InputSequenceNode, InputSequenceNode.Effective>,
		DataNodeChildNode<InputSequenceNode, InputSequenceNode.Effective> {
	interface Effective extends InputSequenceNode,
			InputNode.Effective<InputSequenceNode, Effective> {
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
