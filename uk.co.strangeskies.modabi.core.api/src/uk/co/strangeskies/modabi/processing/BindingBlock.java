package uk.co.strangeskies.modabi.processing;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataSource;

/**
 * A {@link BindingBlock} is intended to represent that a binding thread is waiting for
 * resources or dependencies to be made available by some external or internal
 * process.
 * <p>
 * Each conceptual block should belong to a unique resource, and be given a
 * unique {@link #namespace()} and {@link #id()}.
 * 
 * @author Elias N Vasylenko
 */
public interface BindingBlock {
	/**
	 * @return The namespace of the blocking resource
	 */
	QualifiedName namespace();

	/**
	 * @return The id of the blocking resource
	 */
	DataSource id();

	/**
	 * @return True if the resource or dependency represented by this block is
	 *         satisfied, false otherwise
	 */
	boolean isComplete();

	/**
	 * @return True if the resource or dependency should be satisfied by processes
	 *         internal to the binding procedure, false otherwise
	 */
	boolean isInternal();

	/**
	 * Signal that the resource or dependency represented by this block has become
	 * available
	 */
	void complete();

	/**
	 * Wait for availability of the resource or dependency
	 * 
	 * @throws InterruptedException
	 *           If the waiting thread is interrupted
	 */
	void waitFor() throws InterruptedException;
}
