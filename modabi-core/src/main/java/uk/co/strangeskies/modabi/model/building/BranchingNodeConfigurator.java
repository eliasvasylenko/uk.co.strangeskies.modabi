package uk.co.strangeskies.modabi.model.building;

import java.util.function.Function;

import uk.co.strangeskies.modabi.model.nodes.BranchingNode;

public interface BranchingNodeConfigurator<S extends BranchingNodeConfigurator<S, N>, N extends BranchingNode>
		extends SchemaNodeConfigurator<S, N> {
	public NodeBuilder addChild();

	public BranchingNodeConfigurator<?, N> addChild(
			Function<NodeBuilder, SchemaNodeConfigurator<?, ?>> nodeBuilder);
}
