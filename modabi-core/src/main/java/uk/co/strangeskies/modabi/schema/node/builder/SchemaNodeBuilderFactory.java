package uk.co.strangeskies.modabi.schema.node.builder;

public interface SchemaNodeBuilderFactory {
	public ElementSchemaNodeBuilder<Object> element();

	public BranchingSchemaNodeBuilder branch();

	public DataSchemaNodeBuilder data();

	public PropertySchemaNodeBuilder property();
}
