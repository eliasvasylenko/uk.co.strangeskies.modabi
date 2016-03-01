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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.processing.BindingFutureBlocker;
import uk.co.strangeskies.modabi.processing.BindingFutureBlocks;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.ObservableImpl;
import uk.co.strangeskies.utilities.Property;
import uk.co.strangeskies.utilities.collection.MultiHashMap;
import uk.co.strangeskies.utilities.collection.MultiMap;
import uk.co.strangeskies.utilities.tuple.Pair;

public class BindingFutureBlocksImpl implements BindingFutureBlocks, BindingFutureBlocker {
	private final ObservableImpl<Pair<QualifiedName, DataSource>> observable = new ObservableImpl<>();
	private final MultiMap<QualifiedName, DataSource, List<DataSource>> blocks = new MultiHashMap<>(ArrayList::new);
	private int internalBlocks = 0;

	private boolean completing = false;

	private Set<Thread> processingThreads = new HashSet<>();

	@Override
	public boolean addObserver(Consumer<? super Pair<QualifiedName, DataSource>> observer) {
		return observable.addObserver(observer);
	}

	@Override
	public boolean removeObserver(Consumer<? super Pair<QualifiedName, DataSource>> observer) {
		return observable.removeObserver(observer);
	}

	@Override
	public <T> T blockAndWaitFor(Supplier<? extends T> blockingSupplier, QualifiedName namespace, DataSource id) {
		synchronized (processingThreads) {
			try {
				return blockAndWaitForImpl(blockingSupplier, namespace, id);
			} finally {
				assertResolvable(false);
			}
		}
	}

	public <T> T blockAndWaitForImpl(Supplier<? extends T> blockingSupplier, QualifiedName namespace, DataSource id) {
		synchronized (processingThreads) {
			blocks.add(namespace, id);
			observable.fire(new Pair<>(namespace, id));

			assertResolvable(false);

			try {
				Property<T, T> result = new IdentityProperty<>();
				Property<Boolean, Boolean> complete = new IdentityProperty<>(false);
				Property<RuntimeException, RuntimeException> exception = new IdentityProperty<>();
				new Thread(() -> {
					try {
						result.set(blockingSupplier.get());
						complete.set(true);
					} catch (RuntimeException e) {
						exception.set(e);
					} finally {
						synchronized (processingThreads) {
							processingThreads.notifyAll();
						}
					}
				}).start();

				while (!complete.get() && exception.get() == null) {
					try {
						processingThreads.wait();
					} catch (InterruptedException e) {}
				}

				if (exception.get() != null) {
					throw exception.get();
				}

				blocks.removeValue(namespace, id);

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
	public <T> T blockAndWaitForInteral(Supplier<? extends T> blockingSupplier, QualifiedName namespace, DataSource id) {
		synchronized (processingThreads) {
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
	public Thread blockForInteral(Runnable blockingRunnable, QualifiedName namespace, DataSource id) {
		synchronized (processingThreads) {
			assertResolvable(true);
		}

		Thread thread = new Thread(() -> {
			blockAndWaitForInteral(() -> {
				blockingRunnable.run();
				return null;
			}, namespace, id);
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
		synchronized (processingThreads) {
			processingThreads.add(processingThread);
			new Thread(() -> {
				try {
					processingThread.join();
				} catch (Exception e) {
					// exception should be handled elsewhere
				} finally {
					synchronized (processingThreads) {
						processingThreads.remove(processingThread);
						processingThreads.notifyAll();
					}
				}
			}).start();
		}
	}

	@Override
	public Set<QualifiedName> waitingForNamespaces() {
		synchronized (processingThreads) {
			return new HashSet<>(blocks.keySet());
		}
	}

	@Override
	public List<DataSource> waitingForIds(QualifiedName namespace) {
		synchronized (processingThreads) {
			return new ArrayList<>(blocks.get(namespace));
		}
	}

	@Override
	public void waitFor(QualifiedName namespace, DataSource id) throws InterruptedException {
		synchronized (processingThreads) {
			while (blocks.contains(namespace, id)) {
				processingThreads.wait();
			}
		}
	}

	@Override
	public void waitFor(QualifiedName namespace, DataSource id, long timeoutMilliseconds) throws InterruptedException {
		synchronized (processingThreads) {
			while (blocks.contains(namespace, id)) {
				processingThreads.wait(timeoutMilliseconds);
			}
		}
	}

	@Override
	public void waitForAll(QualifiedName namespace) throws InterruptedException {
		synchronized (processingThreads) {
			while (blocks.containsKey(namespace)) {
				processingThreads.wait();
			}
		}
	}

	@Override
	public void waitForAll(QualifiedName namespace, long timeoutMilliseconds) throws InterruptedException {
		synchronized (processingThreads) {
			while (blocks.containsKey(namespace)) {
				processingThreads.wait(timeoutMilliseconds);
			}
		}
	}

	@Override
	public void waitForAll() throws InterruptedException {
		synchronized (processingThreads) {
			while (!blocks.isEmpty()) {
				processingThreads.wait();
			}
		}
	}

	@Override
	public void waitForAll(long timeoutMilliseconds) throws InterruptedException {
		synchronized (processingThreads) {
			while (!blocks.isEmpty()) {
				processingThreads.wait(timeoutMilliseconds);
			}
		}
	}

	@Override
	public boolean isBlocked() {
		synchronized (processingThreads) {
			return !blocks.isEmpty();
		}
	}

	public void complete() throws InterruptedException {
		synchronized (processingThreads) {
			completing = true;
			assertResolvable(false);
			waitForAll();
		}
	}
}
