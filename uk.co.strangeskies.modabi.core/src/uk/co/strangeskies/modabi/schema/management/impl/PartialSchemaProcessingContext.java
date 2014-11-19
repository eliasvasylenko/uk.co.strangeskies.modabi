package uk.co.strangeskies.modabi.schema.management.impl;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.management.SchemaProcessingContext;
import uk.co.strangeskies.modabi.schema.node.BindingChildNode;
import uk.co.strangeskies.modabi.schema.node.ChoiceNode;
import uk.co.strangeskies.modabi.schema.node.ComplexNode;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.InputNode;
import uk.co.strangeskies.modabi.schema.node.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.SequenceNode;

public interface PartialSchemaProcessingContext extends SchemaProcessingContext {
	@Override
	default <U> void accept(ComplexNode.Effective<U> node) {
		accept((BindingChildNode.Effective<U, ?, ?>) node);
	}

	@Override
	default <U> void accept(DataNode.Effective<U> node) {
		accept((BindingChildNode.Effective<U, ?, ?>) node);
	}

	@Override
	default void accept(InputSequenceNode.Effective node) {
		accept((InputNode.Effective<?, ?>) node);
	}

	@Override
	default void accept(SequenceNode.Effective node) {
		unexpectedNode(node);
	}

	@Override
	default void accept(ChoiceNode.Effective node) {
		unexpectedNode(node);
	}

	default <U> void accept(BindingChildNode.Effective<U, ?, ?> node) {
		accept((InputNode.Effective<?, ?>) node);
	}

	default void accept(InputNode.Effective<?, ?> node) {
		unexpectedNode(node);
	}

	static void unexpectedNode(SchemaNode<?, ?> node) {
		throw new SchemaException("Unexpected node type '"
				+ node.getEffectiveClass() + "' for node '" + node.getName() + "'.");
	}
}
