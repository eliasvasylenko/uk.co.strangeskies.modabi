package uk.co.strangeskies.modabi.model;

import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public interface SchemaNode {
	public String getId();

	public void process(SchemaProcessingContext context);
}
