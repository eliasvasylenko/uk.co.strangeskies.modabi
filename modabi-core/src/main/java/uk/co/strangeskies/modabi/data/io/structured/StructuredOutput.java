package uk.co.strangeskies.modabi.data.io.structured;

import uk.co.strangeskies.modabi.data.io.TerminatingDataTarget;

public interface StructuredOutput {
	public void childElement(String name);

	public TerminatingDataTarget property(String name);

	public TerminatingDataTarget content();

	public void endElement();
}
