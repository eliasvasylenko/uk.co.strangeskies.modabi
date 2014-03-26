package uk.co.strangeskies.modabi.model.building;

public interface ChildBuilder {
	public ElementNodeConfigurator<Object> element();

	public SequenceNodeConfigurator sequence();

	public ChoiceNodeConfigurator choice();

	public DataNodeConfigurator<Object> data();
}
