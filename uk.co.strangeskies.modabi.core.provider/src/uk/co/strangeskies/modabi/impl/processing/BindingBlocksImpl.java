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

import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.processing.BindingBlocker;
import uk.co.strangeskies.modabi.processing.BindingBlocks;
import uk.co.strangeskies.modabi.processing.BindingBlock;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.ObservableImpl;
import uk.co.strangeskies.utilities.Property;
import uk.co.strangeskies.utilities.collection.MultiHashMap;
import uk.co.strangeskies.utilities.collection.MultiMap;

public class BindingBlocksImpl implements BindingBlocks, BindingBlocker {
	private final ObservableImpl<BindingBlock> observable = new ObservableImpl<>();
	private final MultiMap<QualifiedName, BindingBlock, Set<BindingBlock>> blocks = new MultiHashMap<>(HashSet::new);
	private int internalBlocks = 0;

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
	public <T> T blockAndWaitFor(Function<BindingBlock, ? extends T> blockingSupplier, QualifiedName namespace,
			DataSource id) {
		synchronized (blocks) {
			try {
				return blockAndWaitForImpl(blockingSupplier, namespace, id);
			} finally {
				assertResolvable(false);
			}
		}
	}

	public <T> T blockAndWaitForImpl(Function<BindingBlock, ? extends T> blockingSupplier, QualifiedName namespace,
			DataSource id) {
		synchronized (blocks) {
			BindingBlock block = new BlockImpl(namespace, id);

			blocks.add(namespace, block);
			observable.fire(block);

			assertResolvable(false);

			try {
				Property<T, T> result = new IdentityProperty<>();
				Property<Boolean, Boolean> complete = new IdentityProperty<>(false);
				Property<RuntimeException, RuntimeException> exception = new IdentityProperty<>();
				new Thread(() -> {
					try {
						result.set(blockingSupplier.apply(block));
						complete.set(true);
					} catch (RuntimeException e) {
						exception.set(e);
					} finally {
						synchronized (blocks) {
							processingThreads.notifyAll();
						}
					}
				}).start();

				while (!complete.get() && exception.get() == null) {
					try {
						blocks.wait();
					} catch (InterruptedException e) {}
				}

				if (exception.get() != null) {
					throw exception.get();
				}

				blocks.removeValue(namespace, block);

				assertResolvable(false);

				return result.get();
			} catch (Exception e) {
				// TODO fail properly, cancel binding future etc.

				throw e;
			} finally {
				processingThreads.notifyAll();
			}
		}
	}

	@Override
	public <T> T blockAndWaitForInternal(Function<BindingBlock, ? extends T> blockingSupplier, QualifiedName namespace,
			DataSource id) {
		synchronized (blocks) {
			boolean addedThread = !processingThreads.contains(Thread.currentThread());
			if (addedThread) {
				processingThreads.add(Thread.currentThread());
			}
			internalBlocks++;

			try {
				T result = blockAndWaitForImpl(blockingSupplier, namespace, id);
				return result;

			} finally {
				if (addedThread) {
					processingThreads.remove(Thread.currentThread());
				}
				internalBlocks--;
			}
		}
	}

	@Override
	public Thread blockForInternal(Consumer<BindingBlock> blockingRunnable, QualifiedName namespace, DataSource id) {
		synchronized (blocks) {
			assertResolvable(true);
		}

		Thread thread = new Thread(() -> {
			blockAndWaitForInternal(blockingRunnable, namespace, id);
		});
		thread.start();
		return thread;
	}

	private void assertResolvable(boolean failIfCompleting) {
		if ((processingThreads.size() <= internalBlocks) || (completing && (internalBlocks > 0 || failIfCompleting))) {
			throw new SchemaException(
					"Internal dependencies unresolvable; waiting for " + internalBlocks + " of resources " + blocks);
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
						processingThreads.notifyAll();
					}
				}
			}).start();
		}
	}

	@Override
	public Set<QualifiedName> getBlockingNamespaces() {
		synchronized (blocks) {
			return new HashSet<>(blocks.keySet());
		}
	}

	@Override
	public Set<BindingBlock> getBlocks(QualifiedName namespace) {
		synchronized (blocks) {
			return new HashSet<>(blocks.get(namespace));
		}
	}

	@Override
	public Set<BindingBlock> getBlocks() {
		synchronized (blocks) {
			return new HashSet<>(blocks.values().stream().flatMap(Collection::stream).collect(toSet()));
		}
	}

	@Override
	public void waitFor(BindingBlock block) throws InterruptedException {
		synchronized (blocks) {
			while (blocks.contains(block.namespace(), block)) {
				blocks.wait();
			}
		}
	}

	@Override
	public void waitFor(BindingBlock block, long timeoutMilliseconds) throws InterruptedException {
		synchronized (blocks) {
			while (blocks.contains(block.namespace(), block)) {
				blocks.wait(timeoutMilliseconds);
			}
		}
	}

	@Override
	public void waitForAll(QualifiedName namespace) throws InterruptedException {
		synchronized (blocks) {
			while (blocks.containsKey(namespace)) {
				blocks.wait();
			}
		}
	}

	@Override
	public void waitForAll(QualifiedName namespace, long timeoutMilliseconds) throws InterruptedException {
		synchronized (blocks) {
			while (blocks.containsKey(namespace)) {
				blocks.wait(timeoutMilliseconds);
			}
		}
	}

	@Override
	public void waitForAll() throws InterruptedException {
		synchronized (blocks) {
			while (!blocks.isEmpty()) {
				blocks.wait();
			}
		}
	}

	@Override
	public void waitForAll(long timeoutMilliseconds) throws InterruptedException {
		synchronized (blocks) {
			while (!blocks.isEmpty()) {
				blocks.wait(timeoutMilliseconds);
			}
		}
	}

	@Override
	public boolean isBlocked() {
		synchronized (blocks) {
			return !blocks.isEmpty();
		}
	}

	public void complete() throws InterruptedException {
		synchronized (blocks) {
			completing = true;
			assertResolvable(false);
			waitForAll();
		}
	}
}
