/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.provider.
 *
 * uk.co.strangeskies.modabi.core.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.impl.processing;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.processing.BindingBlock;
import uk.co.strangeskies.modabi.processing.BindingBlocker;
import uk.co.strangeskies.modabi.processing.BindingBlocks;
import uk.co.strangeskies.utilities.ObservableImpl;

public class BindingBlocksImpl implements BindingBlocks, BindingBlocker {
	private final ObservableImpl<BindingBlock> observable = new ObservableImpl<>();
	private final Set<BindingBlock> blocks = new HashSet<>();
	private int internalBlockCount = 0;

	private boolean completing = false;

	private Set<Thread> processingThreads = new HashSet<>();

	@Override
	public boolean addObserver(Consumer<? super BindingBlock> observer) {
		return observable.addObserver(observer);
	}

	@Override
	public boolean removeObserver(Consumer<? super BindingBlock> observer) {
		return observable.removeObserver(observer);
	}

	@Override
	public BindingBlock block(QualifiedName namespace, DataSource id, boolean internal) {
		BindingBlock block = new BindingBlockImpl(namespace, id, internal);

		synchronized (blocks) {
			blocks.add(block);

			return block;
		}
	}

	private void assertResolvable(boolean failIfCompleting) {
		if ((processingThreads.size() <= internalBlockCount)
				|| (completing && (internalBlockCount > 0 || failIfCompleting))) {
			throw new SchemaException(
					"Internal dependencies unresolvable; waiting for " + internalBlockCount + " of resources " + blocks);
		}
	}

	@Override
	public void addInternalProcessingThread(Thread processingThread) {
		synchronized (blocks) {
			processingThreads.add(processingThread);
			new Thread(() -> {
				try {
					processingThread.join();
				} catch (Exception e) {
					// exception should be handled elsewhere
				} finally {
					synchronized (blocks) {
						processingThreads.remove(processingThread);
						blocks.notifyAll();
					}
				}
			}).start();
		}
	}

	@Override
	public Set<BindingBlock> getBlocks() {
		synchronized (blocks) {
			return new HashSet<>(blocks);
		}
	}

	@Override
	public void waitForAll() throws InterruptedException, ExecutionException {
		do {
			BindingBlock block;

			synchronized (blocks) {
				if (isBlocked()) {
					block = blocks.iterator().next();
				} else {
					break;
				}
			}

			block.waitUntilComplete();
		} while (true);
	}

	@Override
	public void waitForAll(long timeoutMilliseconds) throws InterruptedException, TimeoutException, ExecutionException {
		long startTime = System.currentTimeMillis();

		do {
			BindingBlock block;

			synchronized (blocks) {
				if (isBlocked()) {
					block = blocks.iterator().next();
				} else {
					break;
				}
			}

			long timeLeft = timeoutMilliseconds + startTime - System.currentTimeMillis();

			block.waitUntilComplete(timeLeft);
		} while (true);
	}

	@Override
	public boolean isBlocked() {
		synchronized (blocks) {
			return !blocks.isEmpty();
		}
	}

	public void complete() throws InterruptedException, ExecutionException {
		synchronized (blocks) {
			completing = true;
			assertResolvable(false);
			waitForAll();
		}
	}
}
