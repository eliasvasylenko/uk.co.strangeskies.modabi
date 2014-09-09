package uk.co.strangeskies.modabi.schema.model.building;

import uk.co.strangeskies.modabi.schema.model.building.configurators.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.DataNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;

public interface ChildBuilder<C extends ChildNode<?, ?>, B extends BindingChildNode<?, ?, ?>> {
	public ElementNodeConfigurator<Object> element();

	public InputSequenceNodeConfigurator<B> inputSequence();

	public SequenceNodeConfigurator<C, B> sequence();

	public ChoiceNodeConfigurator<C, B> choice();

	public DataNodeConfigurator<Object> data();
}
