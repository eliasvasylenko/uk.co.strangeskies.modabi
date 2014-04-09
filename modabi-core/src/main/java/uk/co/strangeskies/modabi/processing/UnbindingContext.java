package uk.co.strangeskies.modabi.processing;

import uk.co.strangeskies.modabi.data.TerminatingDataSink;

public interface UnbindingContext {
	public Object getTarget();

	public TerminatingDataSink property(String id);

	public TerminatingDataSink simpleElement(String id);

	public TerminatingDataSink content();

	public void endData();
}
