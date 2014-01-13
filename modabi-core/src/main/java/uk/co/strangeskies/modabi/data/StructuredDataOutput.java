package uk.co.strangeskies.modabi.data;

public interface StructuredDataOutput {
	public void childElement(String name);

	public TerminatingDataSink property(String name);

	public TerminatingDataSink content();

	public void endElement();
}
