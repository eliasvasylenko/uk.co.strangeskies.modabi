package uk.co.strangeskies.modabi.node.builder;

public interface SchemaNodeBuilderFactory {
	public BindingNodeBuilder<Object> element();

	public BranchNodeBuilder branch();

	public DataNodeBuilder<Object> data();

	public PropertyNodeBuilder<Object> property();
}
