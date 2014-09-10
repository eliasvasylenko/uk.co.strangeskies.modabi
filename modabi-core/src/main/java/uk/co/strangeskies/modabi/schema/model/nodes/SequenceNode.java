package uk.co.strangeskies.modabi.schema.model.nodes;

import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface SequenceNode extends
		ChildNode<SequenceNode, SequenceNode.Effective>,
		DataNodeChildNode<SequenceNode, SequenceNode.Effective> {
	interface Effective extends SequenceNode,
			ChildNode.Effective<SequenceNode, Effective> {
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
	default Class<SequenceNode> getNodeClass() {
		return SequenceNode.class;
	}
}
