package uk.co.strangeskies.modabi.schema.node.builder;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.schema.node.ElementSchemaNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;

public interface ElementSchemaNodeBuilder<T> extends
		SchemaNodeBuilder<ElementSchemaNode<? extends T>> {
	public ElementSchemaNodeBuilder<T> name(String name);

	public ElementSchemaNodeBuilder<T> base(ElementSchemaNode<? super T> base);

	public ElementSchemaNodeBuilder<T> occurances(Range<Integer> occuranceRange);

	public <U extends T> ElementSchemaNodeBuilder<U> dataClass(Class<U> dataClass);

	public ElementSchemaNodeBuilder<T> factoryClass(Class<?> factoryClass);

	public ElementSchemaNodeBuilder<T> addChild(SchemaNode child);

	public ElementSchemaNodeBuilder<T> choice(boolean isChoice);

	public ElementSchemaNodeBuilder<T> inMethod(String inMethodName);

	public ElementSchemaNodeBuilder<T> outMethod(String outMethodName);

	public ElementSchemaNodeBuilder<T> iterable(boolean isIterable);

	public ElementSchemaNodeBuilder<T> buildMethod(String buildMethodName);
}
