package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.processing.ProcessingException;

public interface BindingConditionEvaluation<T> {
	void processNext(T binding);

	/**
	 * @param context
	 *          the current state of some binding process
	 * @throws ProcessingException
	 *           If it is not permitted to process the associated binding point
	 *           for the given processing state an appropriate exception is
	 *           thrown.
	 */
	boolean canContinueProcess();

	/**
	 * @param context
	 *          the current state of some binding process
	 * @throws ProcessingException
	 *           If it is not permitted to skip processing of the associated
	 *           binding point for the given processing state an appropriate
	 *           exception is thrown.
	 * 
	 */
	boolean canEndProcess();

	ProcessingException failProcess();
}
