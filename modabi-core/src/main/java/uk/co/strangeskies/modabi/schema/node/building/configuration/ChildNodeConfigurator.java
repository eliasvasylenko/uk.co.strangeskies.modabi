package uk.co.strangeskies.modabi.schema.node.building.configuration;

import uk.co.strangeskies.modabi.schema.node.BindingChildNode;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;

public interface ChildNodeConfigurator<S extends ChildNodeConfigurator<S, N, C, B>, N extends SchemaNode<?, ?>, C extends ChildNode<?, ?>, B extends BindingChildNode<?, ?, ?>>
		extends SchemaNodeConfigurator<S, N, C, B> {
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
