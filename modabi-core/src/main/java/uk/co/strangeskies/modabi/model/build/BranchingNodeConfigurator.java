package uk.co.strangeskies.modabi.model.build;

import java.util.function.Function;

import uk.co.strangeskies.modabi.model.BranchingNode;

public interface BranchingNodeConfigurator<S extends BranchingNodeConfigurator<S, N>, N extends BranchingNode>
		extends SchemaNodeConfigurator<S, N> {
	public NodeBuilder addChild();

	public BranchingNodeConfigurator<S, N> addChild(
			Function<NodeBuilder, SchemaNodeConfigurator<?, ?>> builder);
}
