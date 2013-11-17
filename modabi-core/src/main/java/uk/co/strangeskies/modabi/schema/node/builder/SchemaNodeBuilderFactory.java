package uk.co.strangeskies.modabi.schema.node.builder;

import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface SchemaNodeBuilderFactory {
	public ElementSchemaNodeBuilder<Object, SchemaProcessingContext<?>> element();

	public BranchSchemaNodeBuilder<SchemaProcessingContext<?>> branch();

	public DataSchemaNodeBuilder<Object> data();

	public PropertySchemaNodeBuilder<Object> property();
}
