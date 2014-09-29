package uk.co.strangeskies.modabi.schema.node.building.configuration;

import uk.co.strangeskies.modabi.schema.node.BindingChildNode;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.SequenceNode;

public interface SequenceNodeConfigurator<C extends ChildNode<?, ?>, B extends BindingChildNode<?, ?, ?>>
		extends
		ChildNodeConfigurator<SequenceNodeConfigurator<C, B>, SequenceNode, C, B>,
		SchemaNodeConfigurator<SequenceNodeConfigurator<C, B>, SequenceNode, C, B> {
}
