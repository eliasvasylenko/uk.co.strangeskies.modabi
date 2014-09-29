package uk.co.strangeskies.modabi.schema.node.building;

import uk.co.strangeskies.modabi.schema.node.BindingChildNode;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.building.configuration.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.DataNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.SequenceNodeConfigurator;

public interface ChildBuilder<C extends ChildNode<?, ?>, B extends BindingChildNode<?, ?, ?>> {
	public ElementNodeConfigurator<Object> element();

	public InputSequenceNodeConfigurator<B> inputSequence();

	public SequenceNodeConfigurator<C, B> sequence();

	public ChoiceNodeConfigurator<C, B> choice();

	public DataNodeConfigurator<Object> data();
}
