/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.api.
 *
 * uk.co.strangeskies.modabi.core.api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.api.  If not, see <http://www.gnu.org/licenses/>.
 */
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
