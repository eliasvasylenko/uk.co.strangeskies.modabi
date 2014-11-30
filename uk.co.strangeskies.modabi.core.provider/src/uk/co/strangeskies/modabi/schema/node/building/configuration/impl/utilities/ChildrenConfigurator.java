package uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities;

import uk.co.strangeskies.modabi.schema.node.building.ChildBuilder;
import uk.co.strangeskies.utilities.factory.Factory;

public interface ChildrenConfigurator extends Factory<ChildrenContainer> {
	public ChildBuilder addChild();
}
