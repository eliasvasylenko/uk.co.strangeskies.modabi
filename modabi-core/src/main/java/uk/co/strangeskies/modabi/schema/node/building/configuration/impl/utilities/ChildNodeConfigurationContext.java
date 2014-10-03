package uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities;

import java.util.List;

import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;

public interface ChildNodeConfigurationContext {
	Namespace namespace();

	List<? extends SchemaNode<?, ?>> overriddenNodes();

	boolean hasInput();

	Class<?> inputTarget();

	Class<?> outputTarget();

	DataLoader loader();

	boolean isAbstract();

	boolean dataContext();
}
