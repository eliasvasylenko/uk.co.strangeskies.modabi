package uk.co.strangeskies.modabi.impl;

import java.util.Collection;

import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.node.BindingNode;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public class BindingSchemaBuilderImpl<T, U extends SchemaProcessingContext<U>>
		implements SchemaBuilder<T, U> {
	@Override
	public Schema<T, U> create() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SchemaBuilder<T, U> includes(
			Collection<? extends Schema<?, ? super U>> includes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SchemaBuilder<T, U> types(
			Collection<? extends DataType<?>> types) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SchemaBuilder<T, U> models(
			Collection<? extends BindingNode<?, ? super U>> models) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <V extends T> SchemaBuilder<V, U> root(
			BindingNode<V, ? super U> rootNode) {
		// TODO Auto-generated method stub
		return null;
	}
}