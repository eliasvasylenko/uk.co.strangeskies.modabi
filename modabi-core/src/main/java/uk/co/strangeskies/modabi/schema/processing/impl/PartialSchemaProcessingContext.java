package uk.co.strangeskies.modabi.schema.processing.impl;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.BindingChildNode;
import uk.co.strangeskies.modabi.schema.node.ChoiceNode;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.ElementNode;
import uk.co.strangeskies.modabi.schema.node.InputNode;
import uk.co.strangeskies.modabi.schema.node.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.SequenceNode;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface PartialSchemaProcessingContext extends SchemaProcessingContext {
	@Override
	default <U> void accept(ElementNode.Effective<U> node) {
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
