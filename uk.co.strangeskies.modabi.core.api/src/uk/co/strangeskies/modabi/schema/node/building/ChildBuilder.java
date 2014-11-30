package uk.co.strangeskies.modabi.schema.node.building;

import uk.co.strangeskies.modabi.schema.node.building.configuration.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.ComplexNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.DataNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.SequenceNodeConfigurator;

public interface ChildBuilder {
	public ComplexNodeConfigurator<Object> complex();

	public InputSequenceNodeConfigurator inputSequence();

	public SequenceNodeConfigurator sequence();

	public ChoiceNodeConfigurator choice();

	public DataNodeConfigurator<Object> data();
}
