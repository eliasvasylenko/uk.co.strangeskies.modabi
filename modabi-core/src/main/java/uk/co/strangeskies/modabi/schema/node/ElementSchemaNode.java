package uk.co.strangeskies.modabi.schema.node;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface ElementSchemaNode<T, U extends SchemaProcessingContext<? extends U>>
		extends BranchingSchemaNode<U> {
	public String getName();

	public ElementSchemaNode<? super T, ? super U> getBase();

	public Range<Integer> getOccurances();

	public Class<T> getDataClass();

	public Class<?> getBuilderClass();

	public String getOutMethod();

	public boolean isIterable();

	public String getBuildMethod();
}
