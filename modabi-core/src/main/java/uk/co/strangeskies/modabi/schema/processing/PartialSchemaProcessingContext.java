package uk.co.strangeskies.modabi.schema.processing;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChoiceNode;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.schema.model.nodes.InputNode;
import uk.co.strangeskies.modabi.schema.model.nodes.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SequenceNode;

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
