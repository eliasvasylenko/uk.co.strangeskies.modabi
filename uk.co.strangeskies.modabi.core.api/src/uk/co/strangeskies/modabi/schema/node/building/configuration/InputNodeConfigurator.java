package uk.co.strangeskies.modabi.schema.node.building.configuration;

import uk.co.strangeskies.modabi.schema.node.InputNode;

public interface InputNodeConfigurator<S extends InputNodeConfigurator<S, N>, N extends InputNode<?, ?>>
		extends ChildNodeConfigurator<S, N> {
	public S inMethod(String methodName);

	public S inMethodChained(boolean chained);

	public S isInMethodCast(boolean isInMethodCast);
}
