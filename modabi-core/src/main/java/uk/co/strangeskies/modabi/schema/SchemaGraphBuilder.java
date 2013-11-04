package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.schema.node.ElementSchemaNode;

public interface SchemaGraphBuilder<T> {
	public SchemaGraphBuilder<T> include(SchemaGraph<?> schemaGraph);

	public SchemaGraphBuilder<T> addModel(ElementSchemaNode<?> model);

	public <U extends T> SchemaGraphBuilder<U> root(ElementSchemaNode<U> rootNode);
}
