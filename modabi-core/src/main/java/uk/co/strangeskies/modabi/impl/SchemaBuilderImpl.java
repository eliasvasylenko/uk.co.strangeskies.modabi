package uk.co.strangeskies.modabi.impl;

import java.util.Collection;

import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.node.BindingNode;

public class SchemaBuilderImpl<T> implements SchemaBuilder<T> {
	@Override
	public Schema<T> create() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SchemaBuilder<T> includes(Collection<? extends Schema<?>> includes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SchemaBuilder<T> types(Collection<? extends DataType<?>> types) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SchemaBuilder<T> models(Collection<? extends BindingNode<?>> models) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <V extends T> SchemaBuilder<V> root(BindingNode<V> rootNode) {
		// TODO Auto-generated method stub
		return null;
	}
}
