package uk.co.strangeskies.modabi.schema.node.builder;

import java.util.Collection;

import uk.co.strangeskies.modabi.schema.node.BranchingNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface BranchingNodeBuilder<S extends BranchingNodeBuilder<S, N, U>, N extends BranchingNode<U>, U extends SchemaProcessingContext<? extends U>>
		extends SchemaNodeBuilder<N, U> {
	public S addChild(SchemaNode<? super U> child);

	public S addChildren(Collection<? extends SchemaNode<? super U>> children);

	public S inMethod(String inMethodName);
}
