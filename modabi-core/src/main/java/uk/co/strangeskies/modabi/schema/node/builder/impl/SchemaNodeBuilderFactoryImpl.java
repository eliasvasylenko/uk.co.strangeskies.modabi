package uk.co.strangeskies.modabi.schema.node.builder.impl;

import uk.co.strangeskies.modabi.schema.node.builder.BranchSchemaNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.DataSchemaNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.ElementSchemaNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.PropertySchemaNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.SchemaNodeBuilderFactory;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public class SchemaNodeBuilderFactoryImpl implements SchemaNodeBuilderFactory {
	@Override
	public ElementSchemaNodeBuilder<Object, SchemaProcessingContext<?>> element() {
		return new ElementSchemaNodeBuilderImpl<>();
	}

	@Override
	public BranchSchemaNodeBuilder<SchemaProcessingContext<?>> branch() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataSchemaNodeBuilder<Object> data() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PropertySchemaNodeBuilder<Object> property() {
		// TODO Auto-generated method stub
		return null;
	}
}
