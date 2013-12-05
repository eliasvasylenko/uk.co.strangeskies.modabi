package uk.co.strangeskies.modabi.node.builder.impl;

import uk.co.strangeskies.modabi.node.builder.BindingNodeBuilder;
import uk.co.strangeskies.modabi.node.builder.BranchNodeBuilder;
import uk.co.strangeskies.modabi.node.builder.DataNodeBuilder;
import uk.co.strangeskies.modabi.node.builder.PropertyNodeBuilder;
import uk.co.strangeskies.modabi.node.builder.SchemaNodeBuilderFactory;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public class SchemaNodeBuilderFactoryImpl implements SchemaNodeBuilderFactory {
	@Override
	public BindingNodeBuilder<Object, SchemaProcessingContext<?>> element() {
		return new ElementSchemaNodeBuilderImpl<>();
	}

	@Override
	public BranchNodeBuilder<SchemaProcessingContext<?>> branch() {
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
