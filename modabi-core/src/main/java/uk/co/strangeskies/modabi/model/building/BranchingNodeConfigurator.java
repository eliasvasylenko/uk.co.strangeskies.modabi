package uk.co.strangeskies.modabi.model.building;

import java.util.function.Function;

import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;

public interface BranchingNodeConfigurator<S extends BranchingNodeConfigurator<S, N, B, C>, N extends SchemaNode, B, C extends ChildNode>
		extends SchemaNodeConfigurator<S, N> {
	public B addChild();

	public default BranchingNodeConfigurator<?, N, B, C> addChild(
			Function<B, SchemaNodeConfigurator<?, ? extends C>> builder) {
		builder.apply(addChild()).create();

		return this;
	}
}
