package uk.co.strangeskies.modabi.schema.node.building.configuration;

import uk.co.strangeskies.modabi.schema.node.BindingChildNode;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.InputNode;

public interface InputNodeConfigurator<S extends InputNodeConfigurator<S, N, C, B>, N extends InputNode<?, ?>, C extends ChildNode<?, ?>, B extends BindingChildNode<?, ?, ?>>
		extends ChildNodeConfigurator<S, N, C, B> {
	public S inMethod(String methodName);

	public S inMethodChained(boolean chained);

	public S isInMethodCast(boolean isInMethodCast);
}
