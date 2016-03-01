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
import java.util.function.Consumer;

import uk.co.strangeskies.utilities.Observable;

public interface BindingBlocks extends Observable<BindingBlock> {
	public static BindingBlocks NON_BLOCKING = new BindingBlocks() {
		@Override
		public boolean addObserver(Consumer<? super BindingBlock> observer) {
			return true;
		}

		@Override
		public boolean removeObserver(Consumer<? super BindingBlock> observer) {
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
	};

	Set<BindingBlock> getBlocks();

	void waitForAll() throws InterruptedException, ExecutionException;

	void waitForAll(long timeoutMilliseconds) throws InterruptedException, TimeoutException, ExecutionException;

	boolean isBlocked();
}
