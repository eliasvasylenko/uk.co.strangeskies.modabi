package uk.co.strangeskies.modabi.model.building;

import java.util.function.Function;

import uk.co.strangeskies.modabi.model.nodes.SchemaNode;

public interface BranchingNodeConfigurator<S extends BranchingNodeConfigurator<S, N>, N extends SchemaNode>
		extends SchemaNodeConfigurator<S, N> {
	public ChildBuilder addChild();

	public default BranchingNodeConfigurator<?, N> addChild(
			Function<ChildBuilder, SchemaNodeConfigurator<?, ?>> builder) {
		builder.apply(addChild()).create();

		return this;
	}
}
