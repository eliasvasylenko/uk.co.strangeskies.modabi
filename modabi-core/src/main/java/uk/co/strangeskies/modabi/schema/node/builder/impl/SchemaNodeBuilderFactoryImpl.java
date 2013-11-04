package uk.co.strangeskies.modabi.schema.node.builder.impl;

import uk.co.strangeskies.modabi.schema.node.builder.BranchingSchemaNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.DataSchemaNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.ElementSchemaNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.PropertySchemaNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.SchemaNodeBuilderFactory;

public class SchemaNodeBuilderFactoryImpl implements SchemaNodeBuilderFactory {
	@Override
	public ElementSchemaNodeBuilder<Object> element() {
		return new ElementSchemaNodeBuilderImpl<Object>();
	}

	@Override
	public BranchingSchemaNodeBuilder branch() {
		return new BranchingSchemaNodeBuilderImpl();
	}

	@Override
	public DataSchemaNodeBuilder data() {
		return new DataSchemaNodeBuilderImpl();
	}

	@Override
	public PropertySchemaNodeBuilder property() {
		return new PropertySchemaNodeBuilderImpl();
	}

}
