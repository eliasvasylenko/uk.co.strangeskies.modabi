package uk.co.strangeskies.modabi.schema.impl;

import uk.co.strangeskies.modabi.schema.BindingSchema;
import uk.co.strangeskies.modabi.schema.BindingSchemaBuilder;
import uk.co.strangeskies.modabi.schema.node.ElementSchemaNode;

public class BindingSchemaBuilderImpl<T> implements BindingSchemaBuilder<T> {
	@Override
	public BindingSchemaBuilder<T> include(BindingSchema<?> schemaGraph) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BindingSchemaBuilder<T> addModel(ElementSchemaNode<?> model) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U extends T> BindingSchemaBuilder<U> root(
			ElementSchemaNode<U> rootNode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BindingSchema<T> create() {
		// TODO Auto-generated method stub
		return null;
	}
}
