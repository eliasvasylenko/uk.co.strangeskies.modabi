package uk.co.strangeskies.modabi.model.impl;

import uk.co.strangeskies.modabi.data.TerminatingDataSink;
import uk.co.strangeskies.modabi.processing.UnbindingContext;

public interface UnbindingChildContext {
	Object getTarget();

	TerminatingDataSink getOpenSink();

	UnbindingContext getUnbindingContext();
}
