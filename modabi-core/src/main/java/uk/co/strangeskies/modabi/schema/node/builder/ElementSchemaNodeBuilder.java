package uk.co.strangeskies.modabi.schema.node.builder;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.schema.node.ElementSchemaNode;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface ElementSchemaNodeBuilder<T, U extends SchemaProcessingContext<? extends U>>
		extends
		BranchingSchemaNodeBuilder<ElementSchemaNodeBuilder<T, U>, ElementSchemaNode<T, U>, U> {
	public ElementSchemaNodeBuilder<T, U> name(String name);

	public <V extends T> ElementSchemaNodeBuilder<V, U> base(
			ElementSchemaNode<? super V, U> base);

	public ElementSchemaNodeBuilder<T, U> occurances(Range<Integer> occuranceRange);

	public <V extends T> ElementSchemaNodeBuilder<V, U> dataClass(
			Class<V> dataClass);

	public ElementSchemaNodeBuilder<T, U> factoryClass(Class<?> factoryClass);

	public ElementSchemaNodeBuilder<T, U> outMethod(String outMethodName);

	public ElementSchemaNodeBuilder<T, U> iterable(boolean isIterable);

	public ElementSchemaNodeBuilder<T, U> buildMethod(String buildMethodName);
}
