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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import uk.co.strangeskies.observable.Observable;
import uk.co.strangeskies.observable.Observer;

public interface BindingBlocks extends Observable<BindingBlockEvent> {
	public static BindingBlocks NON_BLOCKING = new BindingBlocks() {
		@Override
		public boolean addObserver(Observer<? super BindingBlockEvent> observer) {
			return true;
		}

		@Override
		public boolean removeObserver(Observer<? super BindingBlockEvent> observer) {
			return true;
		}

		@Override
		public Set<BindingBlock> getBlocks() {
			return Collections.emptySet();
		}

		@Override
		public void waitForAll() throws InterruptedException {}

		@Override
		public void waitForAll(long timeoutMilliseconds) throws InterruptedException, TimeoutException {}

		@Override
		public boolean isBlocked() {
			return false;
		}

		@Override
		public boolean isDeadlocked() {
			return false;
		}
	};

	/**
	 * @return All blocks in the binding process which are still incomplete
	 */
	Set<BindingBlock> getBlocks();

	/**
	 * Wait until no blocks remain held in the binding system. This may not
	 * signify the end of binding, or that more blocks will not occur.
	 * 
	 * @throws InterruptedException
	 *           The waiting thread was interrupted
	 * @throws ExecutionException
	 *           The blocking dependency failed resolution with the causing
	 *           exception
	 */
	void waitForAll() throws InterruptedException, ExecutionException;

	/**
	 * Wait until no blocks remain held in the binding system, or until the given
	 * time period ends. Successful invocation may not signify the end of binding,
	 * or that more blocks will not occur.
	 * 
	 * @throws InterruptedException
	 *           The waiting thread was interrupted
	 * @throws ExecutionException
	 *           The blocking dependency failed resolution with the causing
	 *           exception
	 * @throws TimeoutException
	 *           The timeout period elapsed before the blocks were resolved
	 */
	void waitForAll(long timeoutMilliseconds) throws InterruptedException, TimeoutException, ExecutionException;

	/**
	 * Determine whether the binding block system as a whole is blocked. It is
	 * considered blocked if every participating {@link Thread} registered with
	 * the associated {@link BindingBlocker} is waiting on a block via
	 * {@link #waitForAll()}, or {@link BindingBlock#waitUntilComplete()}, etc.
	 * <p>
	 * Note: If this is the case, and it is also the case that all threads are
	 * blocked on internal, not external, dependencies, the binding will fail.
	 * 
	 * @return True if the binding is blocked and no processing threads are
	 *         active, false otherwise
	 */
	boolean isBlocked();

	public boolean isDeadlocked();
}
