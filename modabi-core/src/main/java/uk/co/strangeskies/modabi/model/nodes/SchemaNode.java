package uk.co.strangeskies.modabi.model.nodes;

import java.util.List;

import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;
import uk.co.strangeskies.modabi.processing.SchemaResultProcessingContext;

public interface SchemaNode {
	public String getId();

	public void process(SchemaProcessingContext context);

	public <T> T process(SchemaResultProcessingContext<T> context);

	public List<? extends ChildNode> getChildren();
}
