package uk.co.strangeskies.modabi.schema.node;

import uk.co.strangeskies.gears.mathematics.Range;

public interface ElementSchemaNode<T> extends BranchingSchemaNode {
	public String getName();

	public ElementSchemaNode<? super T> getBase();

	public Range<Integer> getOccurances();

	public Class<T> getDataClass();

	public Class<?> getBuildClass();

	public String getOutMethod();

	public boolean isIterable();

	public String getBuildMethod();
}
