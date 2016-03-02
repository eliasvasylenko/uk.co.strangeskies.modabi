package uk.co.strangeskies.modabi.processing;

public interface BindingBlockEvent {
	/**
	 * The type of the {@link BindingBlock} event.
	 * 
	 * @author Elias N Vasylenko
	 */
	enum Type {
		/**
		 * The block was started, signifying that a dependency has been determined
		 * and must be fulfilled.
		 */
		STARTED,

		/**
		 * A thread has waited for the block to end.
		 */
		THREAD_BLOCKED,

		/**
		 * A thread which was waiting for the block to end has stopped waiting
		 * prematurely due to timeout.
		 */
		THREAD_UNBLOCKED,

		/**
		 * The block has ended, meaning either the dependency was successfully
		 * fetched, or a failure occurred.
		 */
		ENDED
	}

	/**
	 * @return The {@link BindingBlock} which the event occurred for
	 */
	BindingBlock block();

	/**
	 * @return The type of the event
	 */
	Type type();

	/**
	 * Get the thread the event occurred on. This may be especially significant in
	 * the case of an event of type {@link Type#THREAD_BLOCKED}, as this will show
	 * which thread is waiting on the block.
	 * <p>
	 * It may be useful to note that the event should always be delivered on this
	 * thread, and so it could be retrieved upon observation via
	 * {@link Thread#currentThread()}, but this method is provided for
	 * convenience.
	 * 
	 * @return The thread the event was triggered from
	 */
	Thread thread();
}
