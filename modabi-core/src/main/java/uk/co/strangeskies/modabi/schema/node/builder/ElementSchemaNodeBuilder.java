package uk.co.strangeskies.modabi.schema.node.builder;

import uk.co.strangeskies.modabi.schema.node.ElementSchemaNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;

public interface ElementSchemaNodeBuilder<T> extends
		SchemaNodeBuilder<ElementSchemaNode<? extends T>> {
	public ElementSchemaNodeBuilder<T> name(String name);

	public ElementSchemaNodeBuilder<T> occurances(Integer from, Integer to);

	public <U extends T> ElementSchemaNodeBuilder<U> dataClass(Class<U> dataClass);

	public ElementSchemaNodeBuilder<T> factoryClass(Class<?> factoryClass);

	public ElementSchemaNodeBuilder<T> addChild(SchemaNode child);

	public ElementSchemaNodeBuilder<T> outMethod(String outMethodName);

	public ElementSchemaNodeBuilder<T> iterable(boolean isIterable);

	public ElementSchemaNodeBuilder<T> buildMethod(String buildMethodName);
}
