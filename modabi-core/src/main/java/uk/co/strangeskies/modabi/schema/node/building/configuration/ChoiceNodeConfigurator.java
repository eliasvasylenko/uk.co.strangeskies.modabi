package uk.co.strangeskies.modabi.schema.node.building.configuration;

import uk.co.strangeskies.modabi.schema.node.BindingChildNode;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.ChoiceNode;

public interface ChoiceNodeConfigurator<C extends ChildNode<?, ?>, B extends BindingChildNode<?, ?, ?>>
		extends
		ChildNodeConfigurator<ChoiceNodeConfigurator<C, B>, ChoiceNode, C, B>,
		SchemaNodeConfigurator<ChoiceNodeConfigurator<C, B>, ChoiceNode, C, B> {
	public ChoiceNodeConfigurator<C, B> mandatory(boolean mandatory);
}
