package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.modabi.model.OptionalNode;

public interface OptionalNodeConfigurator<S extends OptionalNodeConfigurator<S, N>, N extends OptionalNode>
		extends SchemaNodeConfigurator<S, N> {
	public S optional(boolean optional);
}
