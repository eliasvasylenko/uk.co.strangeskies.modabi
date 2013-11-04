package uk.co.strangeskies.modabi.schema;

import java.util.Set;

import uk.co.strangeskies.modabi.schema.node.ElementSchemaNode;

public interface SchemaGraph<T> {
	public Set<ElementSchemaNode<?>> getModelSet();

	public ElementSchemaNode<T> getRoot();
}
