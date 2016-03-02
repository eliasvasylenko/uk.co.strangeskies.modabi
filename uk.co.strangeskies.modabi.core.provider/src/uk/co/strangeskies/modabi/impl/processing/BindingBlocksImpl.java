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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.processing.BindingBlock;
import uk.co.strangeskies.modabi.processing.BindingBlocker;
import uk.co.strangeskies.utilities.ObservableImpl;

public class BindingBlocksImpl implements BindingBlocker {
	private final ObservableImpl<BindingBlock> observable = new ObservableImpl<>();
	private final Set<BindingBlock> blocks = new HashSet<>();

	private Set<Thread> processingThreads = new HashSet<>();
	private Map<Thread, BindingBlock> processingThreadBlocks = new HashMap<>();

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
		BindingBlock block = new BindingBlockImpl(namespace, id, internal, this::startThreadBlock, this::endThreadBlock);

		synchronized (blocks) {
			blocks.add(block);
			observable.fire(block);

			return block;
		}
	}

	void startThreadBlock(BindingBlock block) {
		synchronized (blocks) {
			if (processingThreads.contains(Thread.currentThread())) {
				processingThreadBlocks.put(Thread.currentThread(), block);

				assertResolvable();
			}
		}
	}

	void endThreadBlock(BindingBlock block) {
		synchronized (blocks) {
			for (Thread processingThread : processingThreads) {
				if (processingThreadBlocks.get(processingThread) == block) {
					processingThreadBlocks.remove(processingThread);
				}
			}
		}
	}

	private void assertResolvable() {
		synchronized (blocks) {
			if (isBlocked() && processingThreadBlocks.values().stream().allMatch(BindingBlock::isInternal)) {
				throw new SchemaException("Internal dependencies unresolvable; waiting for " + processingThreadBlocks.values());
			}
		}
	}

	@Override
	public void addParticipatingThread(Thread processingThread) {
		synchronized (blocks) {
			processingThreads.add(processingThread);

			new Thread(() -> {
				try {
					try {
						processingThread.join();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				} finally {
					synchronized (blocks) {
						processingThreads.remove(processingThread);

						assertResolvable();
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
				if (!blocks.isEmpty()) {
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
				if (!blocks.isEmpty()) {
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
			return processingThreadBlocks.size() > 0 && processingThreadBlocks.size() == processingThreads.size();
		}
	}

	public void complete() throws InterruptedException, ExecutionException {
		synchronized (blocks) {
			new Thread(() -> {
				assertResolvable();
			});
			waitForAll();
		}
	}
}
