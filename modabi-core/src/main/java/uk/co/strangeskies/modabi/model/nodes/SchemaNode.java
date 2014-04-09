package uk.co.strangeskies.modabi.model.nodes;

import java.util.List;

import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public interface SchemaNode {
	String getId();

	List<? extends ChildNode> getChildren();

	void process(SchemaProcessingContext context);
}
