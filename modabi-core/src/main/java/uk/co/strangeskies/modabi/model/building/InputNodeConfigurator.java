package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.modabi.model.InputNode;

public interface InputNodeConfigurator<S extends InputNodeConfigurator<S, N>, N extends InputNode>
		extends SchemaNodeConfigurator<S, N> {
	public S inMethod(String methodName);

	public S inMethodChained(boolean chained);
}
