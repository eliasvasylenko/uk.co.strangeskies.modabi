package uk.co.strangeskies.modabi.model.building.configurators;

import uk.co.strangeskies.gears.utilities.factory.Factory;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;

public interface SchemaNodeConfigurator<S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode<?, ?>>
		extends Factory<N> {
	public S name(String name);
}
