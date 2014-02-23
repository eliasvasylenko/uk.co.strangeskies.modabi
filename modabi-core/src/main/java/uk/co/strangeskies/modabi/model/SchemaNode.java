package uk.co.strangeskies.modabi.model;

import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public interface SchemaNode {
	public String getId();

	public default <T> T process(SchemaProcessingContext<T> context) {
		throw new UnsupportedOperationException();
	}
}
