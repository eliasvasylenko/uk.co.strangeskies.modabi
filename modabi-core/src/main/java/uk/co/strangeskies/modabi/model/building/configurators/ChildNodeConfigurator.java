package uk.co.strangeskies.modabi.model.building.configurators;

import uk.co.strangeskies.modabi.model.nodes.SchemaNode;

public interface ChildNodeConfigurator<S extends ChildNodeConfigurator<S, N>, N extends SchemaNode<?, ?>>
		extends SchemaNodeConfigurator<S, N> {
	public S name(String name);
}
