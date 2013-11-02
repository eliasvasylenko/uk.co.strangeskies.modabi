package uk.co.strangeskies.modabi;

import java.util.List;

public interface BranchingDeclarationNode extends DeclarationNode {
	public List<DeclarationNode> getChildren();

	public boolean isChoice();

	public String getInMethod();
}
