package uk.co.strangeskies.modabi.schema.node.building.configuration;

import uk.co.strangeskies.modabi.schema.node.SchemaNode;

public interface ChildNodeConfigurator<S extends ChildNodeConfigurator<S, N>, N extends SchemaNode<?, ?>>
		extends SchemaNodeConfigurator<S, N> {
	/**
	 * Here we can just provide a string name instead of a fully qualified name,
	 * and the namespace of the parent node will be used.
	 *
	 * @param name
	 * @return
	 */
	public S name(String name);

	public S postInputClass(Class<?> postInputClass);
}
