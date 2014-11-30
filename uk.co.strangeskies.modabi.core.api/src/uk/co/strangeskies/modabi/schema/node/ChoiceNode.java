package uk.co.strangeskies.modabi.schema.node;

import uk.co.strangeskies.modabi.schema.management.SchemaProcessingContext;

public interface ChoiceNode extends
		ChildNode<ChoiceNode, ChoiceNode.Effective>,
		DataNodeChildNode<ChoiceNode, ChoiceNode.Effective> {
	interface Effective extends ChoiceNode,
			ChildNode.Effective<ChoiceNode, Effective> {
		@Override
		default void process(SchemaProcessingContext context) {
			context.accept(this);
		}
	}

	@Override
	default Class<Effective> getEffectiveClass() {
		return ChoiceNode.Effective.class;
	}

	@Override
	default Class<ChoiceNode> getNodeClass() {
		return ChoiceNode.class;
	}

	Boolean isMandatory();
}
