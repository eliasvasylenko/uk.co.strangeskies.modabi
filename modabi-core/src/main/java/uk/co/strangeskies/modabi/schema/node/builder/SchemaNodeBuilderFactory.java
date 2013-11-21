package uk.co.strangeskies.modabi.schema.node.builder;

import uk.co.strangeskies.modabi.schema.processing.DataInput;

public interface SchemaNodeBuilderFactory {
	public BindingNodeBuilder<Object, DataInput<?>> element();

	public BranchNodeBuilder<DataInput<?>> branch();

	public DataNodeBuilder<Object> data();

	public PropertyNodeBuilder<Object> property();
}
