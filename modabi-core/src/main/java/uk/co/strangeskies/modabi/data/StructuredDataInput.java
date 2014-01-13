package uk.co.strangeskies.modabi.data;

public interface StructuredDataInput {
	public String nextChildElement();

	public DataSource processProperty(String name);

	public DataSource processContent();

	public void endElement();
}
