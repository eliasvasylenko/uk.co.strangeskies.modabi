package uk.co.strangeskies.modabi.schema.node.builder;

import java.util.Collection;

import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface BranchingSchemaNodeBuilder<S extends BranchingSchemaNodeBuilder<S, N, U>, N extends SchemaNode<U>, U extends SchemaProcessingContext<? extends U>>
		extends SchemaNodeBuilder<N, U> {
	public S addChild(SchemaNode<? super U> child);

	public S addChildren(Collection<? extends SchemaNode<? super U>> children);

	public S inMethod(String inMethodName);
}
