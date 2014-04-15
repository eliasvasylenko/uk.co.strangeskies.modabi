package uk.co.strangeskies.modabi.model.nodes;

import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public interface ChildNode extends SchemaNode {
	public Class<?> getPreInputClass();

	public Class<?> getPostInputClass();

	void process(SchemaProcessingContext context);
}
