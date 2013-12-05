package uk.co.strangeskies.modabi.node;

import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public interface InputNode<T extends SchemaProcessingContext<? extends T>>
		extends SchemaNode<T> {
	public String getInMethod();

	/**
	 * If this method returns true, the return value of any invocation of the
	 * inMethod will replace the build class of any
	 * 
	 * @return
	 */
	public boolean isInMethodChained();
}
