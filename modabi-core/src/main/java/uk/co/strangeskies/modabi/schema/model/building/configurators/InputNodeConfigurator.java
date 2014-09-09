package uk.co.strangeskies.modabi.schema.model.building.configurators;

import uk.co.strangeskies.modabi.schema.model.nodes.InputNode;

public interface InputNodeConfigurator<S extends InputNodeConfigurator<S, N>, N extends InputNode<?, ?>>
		extends SchemaNodeConfigurator<S, N> {
	public S inMethod(String methodName);

	public S inMethodChained(boolean chained);
}
