package uk.co.strangeskies.modabi.model.building;

public interface NodeBuilder {
	public ElementNodeConfigurator<Object> element();

	public SequenceNodeConfigurator sequence();

	public ChoiceNodeConfigurator choice();

	public SimpleElementNodeConfigurator<Object> simpleElement();

	public ContentNodeConfigurator<Object> content();

	public PropertyNodeConfigurator<Object> property();
}
