package uk.co.strangeskies.modabi;

import uk.co.strangeskies.gears.mathematics.Range;

public interface ElementDeclarationNode extends BranchingDeclarationNode {
	public String getName();

	public Range<Integer> getOccurances();

	public Class<?> getRequestedClass();

	public String getOutMethod();

	public boolean isIterable();

	public String getBuildMethod();
}
