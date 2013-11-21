package uk.co.strangeskies.modabi.schema.node.builder.impl;

import uk.co.strangeskies.modabi.schema.node.builder.BranchNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.DataNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.BindingNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.PropertyNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.SchemaNodeBuilderFactory;
import uk.co.strangeskies.modabi.schema.processing.DataInput;

public class SchemaNodeBuilderFactoryImpl implements SchemaNodeBuilderFactory {
	@Override
	public BindingNodeBuilder<Object, DataInput<?>> element() {
		return new ElementSchemaNodeBuilderImpl<>();
	}

	@Override
	public BranchNodeBuilder<DataInput<?>> branch() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataNodeBuilder<Object> data() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PropertyNodeBuilder<Object> property() {
		// TODO Auto-generated method stub
		return null;
	}
}
