package uk.co.strangeskies.modabi.node.builder;

import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public interface SchemaNodeBuilderFactory {
	public BindingNodeBuilder<Object, SchemaProcessingContext<?>> element();

	public BranchNodeBuilder<SchemaProcessingContext<?>> branch();

	public DataNodeBuilder<Object> data();

	public PropertyNodeBuilder<Object> property();
}
