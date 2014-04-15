package uk.co.strangeskies.modabi.data.io.structured;

import uk.co.strangeskies.modabi.data.io.DataSource;

public interface StructuredDataInput {
	public String nextChildElement();

	public DataSource processProperty(String name);

	public DataSource processContent();

	public void endElement();

	public <T extends StructuredDataOutput> T pipe(T output);
}
