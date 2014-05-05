package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;

public interface ChildBuilder<C extends ChildNode, B extends BindingChildNode<?>> {
	public ElementNodeConfigurator<Object> element();

	public InputSequenceNodeConfigurator<B> inputSequence();

	public SequenceNodeConfigurator<C, B> sequence();

	public ChoiceNodeConfigurator<C, B> choice();

	public DataNodeConfigurator<Object> data();
}
