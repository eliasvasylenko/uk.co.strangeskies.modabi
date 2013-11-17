package uk.co.strangeskies.modabi.schema.impl;

import java.util.Collection;

import uk.co.strangeskies.modabi.schema.BindingSchema;
import uk.co.strangeskies.modabi.schema.BindingSchemaBuilder;
import uk.co.strangeskies.modabi.schema.node.ElementSchemaNode;
import uk.co.strangeskies.modabi.schema.node.data.SchemaDataType;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public class BindingSchemaBuilderImpl<T, U extends SchemaProcessingContext<U>>
		implements BindingSchemaBuilder<T, U> {
	@Override
	public BindingSchema<T, U> create() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BindingSchemaBuilder<T, U> includes(
			Collection<? extends BindingSchema<?, ? super U>> includes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BindingSchemaBuilder<T, U> types(
			Collection<? extends SchemaDataType<?>> types) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BindingSchemaBuilder<T, U> models(
			Collection<? extends ElementSchemaNode<?, ? super U>> models) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <V extends T> BindingSchemaBuilder<V, U> root(
			ElementSchemaNode<V, ? super U> rootNode) {
		// TODO Auto-generated method stub
		return null;
	}
}
