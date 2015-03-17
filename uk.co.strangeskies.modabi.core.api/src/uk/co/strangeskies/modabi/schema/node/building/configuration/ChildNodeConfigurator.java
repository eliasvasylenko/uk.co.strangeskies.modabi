package uk.co.strangeskies.modabi.schema.node.building.configuration;

import java.lang.reflect.Type;

import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;

public interface ChildNodeConfigurator<S extends ChildNodeConfigurator<S, N>, N extends ChildNode<?, ?>>
		extends SchemaNodeConfigurator<S, N> {
	/**
	 * Here we can just provide a string name instead of a fully qualified name,
	 * and the namespace of the parent node will be used.
	 *
	 * @param name
	 * @return
	 */
	public S name(String name);

	public S postInputType(Type postInputType);
}
