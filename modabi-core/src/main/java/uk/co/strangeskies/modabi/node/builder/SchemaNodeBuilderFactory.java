package uk.co.strangeskies.modabi.schema.node.builder;

import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface SchemaNodeBuilderFactory {
	public BindingNodeBuilder<Object, SchemaProcessingContext<?>> element();

	public BranchNodeBuilder<SchemaProcessingContext<?>> branch();

	public DataNodeBuilder<Object> data();

	public PropertyNodeBuilder<Object> property();
}
