package uk.co.strangeskies.modabi.node.builder;

import java.util.Collection;

import uk.co.strangeskies.modabi.node.BranchingNode;
import uk.co.strangeskies.modabi.node.SchemaNode;

public interface BranchingNodeBuilder<S extends BranchingNodeBuilder<S, N>, N extends BranchingNode>
		extends SchemaNodeBuilder<N> {
	public S addChild(SchemaNode child);

	public S addChildren(Collection<? extends SchemaNode> children);

	public S inMethod(String inMethodName);
}
