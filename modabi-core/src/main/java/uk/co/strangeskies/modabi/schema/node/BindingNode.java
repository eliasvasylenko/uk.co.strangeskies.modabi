package uk.co.strangeskies.modabi.schema.node;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface BindingNode<T, U extends SchemaProcessingContext<? extends U>>
		extends BranchingNode<U> {
	public String getName();

	public BindingNode<? super T, ? super U> getBase();

	public Range<Integer> getOccurances();

	public Class<T> getBindingClass();

	public Class<?> getBuilderClass();

	public String getOutMethod();

	public boolean isIterable();

	/**
	 * The method to call to instantiate the data object in the case that this
	 * element node has an associated builder class as returned by
	 * {@link #getBuilderClass()}. If no build method is specified, this method
	 * will return null. In that case the very last child of this class which has
	 * an associated inMethod will be checked for an appropriate
	 * 
	 * @return
	 */
	public String getBuildMethod();
}
