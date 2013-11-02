package uk.co.strangeskies.modabi;

public interface DataDeclarationNode extends DeclarationNode {
	public DataNodeType<?> getType();

	public boolean isOptional();
}
