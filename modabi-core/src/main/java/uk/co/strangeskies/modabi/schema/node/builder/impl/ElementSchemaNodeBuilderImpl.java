package uk.co.strangeskies.modabi.schema.node.builder.impl;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.schema.node.ElementSchemaNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.builder.ElementSchemaNodeBuilder;

public class ElementSchemaNodeBuilderImpl<T> implements
		ElementSchemaNodeBuilder<T> {
	@Override
	public ElementSchemaNodeBuilder<T> name(String name) {
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U extends T> ElementSchemaNodeBuilder<U> dataClass(Class<U> dataClass) {
		return (ElementSchemaNodeBuilder<U>) this;
	}

	@Override
	public ElementSchemaNodeBuilder<T> addChild(SchemaNode child) {
		return this;
	}

	@Override
	public ElementSchemaNode<T> create() {
		return null;
	}

	@Override
	public ElementSchemaNodeBuilder<T> occurances(Range<Integer> occurances) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ElementSchemaNodeBuilder<T> factoryClass(Class<?> factoryClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ElementSchemaNodeBuilder<T> outMethod(String outMethodName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ElementSchemaNodeBuilder<T> iterable(boolean isIterable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ElementSchemaNodeBuilder<T> buildMethod(String buildMethodName) {
		// TODO Auto-generated method stub
		return null;
	}
}
