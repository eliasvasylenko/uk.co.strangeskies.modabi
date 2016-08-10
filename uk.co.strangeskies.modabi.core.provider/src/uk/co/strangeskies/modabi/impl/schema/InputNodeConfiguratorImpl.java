package uk.co.strangeskies.modabi.impl.schema;

import java.util.List;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.impl.schema.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.InputNode;
import uk.co.strangeskies.modabi.schema.InputNodeConfigurator;

public interface InputNodeConfiguratorImpl<S extends InputNodeConfigurator<S, N>, N extends InputNode<N>>
		extends InputNodeConfigurator<S, N> {
	List<N> getOverriddenNodes();

	N getResult();

	SchemaNodeConfigurationContext getContext();

	default void checkInputAllowed() {
		if (!getContext().isInputExpected())
			throw new ModabiException(t -> t.cannotDefineInputInContext(getName()));
	}
}
