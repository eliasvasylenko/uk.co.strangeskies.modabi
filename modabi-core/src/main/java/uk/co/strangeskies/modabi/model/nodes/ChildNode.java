package uk.co.strangeskies.modabi.model.nodes;

public interface ChildNode extends SchemaNode {
	public Class<?> getPreInputClass();

	public Class<?> getPostInputClass();
}
