package uk.co.strangeskies.modabi.data;

public interface StructuredDataOutput {
	public void childElement(String name);

	public TerminatingDataTarget property(String name);

	public TerminatingDataTarget content();

	public void endElement();
}
