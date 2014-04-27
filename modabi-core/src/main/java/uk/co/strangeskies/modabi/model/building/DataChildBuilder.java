package uk.co.strangeskies.modabi.model.building;

public interface DataChildBuilder {
	public SequenceNodeConfigurator sequence();

	public InputSequenceNodeConfigurator inputSequence();

	public ChoiceNodeConfigurator choice();

	public DataNodeConfigurator<Object> data();
}
