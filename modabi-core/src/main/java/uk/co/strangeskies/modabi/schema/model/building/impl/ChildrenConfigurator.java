package uk.co.strangeskies.modabi.schema.model.building.impl;

import uk.co.strangeskies.modabi.schema.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;
import uk.co.strangeskies.utilities.factory.Factory;

public interface ChildrenConfigurator<C extends ChildNode<?, ?>, B extends BindingChildNode<?, ?, ?>>
		extends Factory<ChildrenContainer> {
	public ChildBuilder<C, B> addChild();
}
