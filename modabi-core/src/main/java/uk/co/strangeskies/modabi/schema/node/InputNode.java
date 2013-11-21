package uk.co.strangeskies.modabi.schema.node;

import uk.co.strangeskies.modabi.schema.processing.DataInput;

public interface InputNode<T extends DataInput<? extends T>>
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
