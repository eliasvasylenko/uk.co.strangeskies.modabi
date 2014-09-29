package uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities;

import uk.co.strangeskies.modabi.schema.node.BindingChildNode;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.building.ChildBuilder;
import uk.co.strangeskies.utilities.factory.Factory;

public interface ChildrenConfigurator<C extends ChildNode<?, ?>, B extends BindingChildNode<?, ?, ?>>
		extends Factory<ChildrenContainer> {
	public ChildBuilder<C, B> addChild();
}
