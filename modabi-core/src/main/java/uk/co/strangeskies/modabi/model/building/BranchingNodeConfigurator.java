package uk.co.strangeskies.modabi.model.building;

import java.util.function.Function;

import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;

public interface BranchingNodeConfigurator<S extends BranchingNodeConfigurator<S, N, C, B>, N extends SchemaNode, C extends ChildNode, B extends BindingChildNode<?>>
		extends SchemaNodeConfigurator<S, N> {
	public ChildBuilder<C, B> addChild();

	public default BranchingNodeConfigurator<?, N, C, B> addChild(
			Function<ChildBuilder<C, B>, SchemaNodeConfigurator<?, ? extends C>> builder) {
		builder.apply(addChild()).create();

		return this;
	}
}
