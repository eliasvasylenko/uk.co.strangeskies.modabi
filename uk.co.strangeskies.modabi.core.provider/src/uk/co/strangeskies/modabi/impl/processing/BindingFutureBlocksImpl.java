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
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.processing.BindingFutureBlocker;
import uk.co.strangeskies.modabi.processing.BindingFutureBlocks;
import uk.co.strangeskies.utilities.ObservableImpl;
import uk.co.strangeskies.utilities.collection.MultiHashMap;
import uk.co.strangeskies.utilities.collection.MultiMap;
import uk.co.strangeskies.utilities.tuple.Pair;

public class BindingFutureBlocksImpl implements BindingFutureBlocks, BindingFutureBlocker {
	private final ObservableImpl<Pair<QualifiedName, DataSource>> observable = new ObservableImpl<>();
	private final MultiMap<QualifiedName, DataSource, List<DataSource>> blocks = new MultiHashMap<>(ArrayList::new);

	@Override
	public boolean addObserver(Consumer<? super Pair<QualifiedName, DataSource>> observer) {
		return observable.addObserver(observer);
	}

	@Override
	public boolean removeObserver(Consumer<? super Pair<QualifiedName, DataSource>> observer) {
		return observable.removeObserver(observer);
	}

	public <T> T blockAndWaitFor(Supplier<? extends T> blockingSupplier, QualifiedName namespace, DataSource id) {
		synchronized (blocks) {
			blocks.add(namespace, id);
			observable.fire(new Pair<>(namespace, id));
		}

		try {
			T result = blockingSupplier.get();

			synchronized (blocks) {
				blocks.remove(namespace, id);
				blocks.notifyAll();
				return result;
			}
		} catch (Exception e) {
			synchronized (blocks) {
				blocks.notifyAll();

				// TODO fail properly, cancel bindingfuture etc.

				throw e;
			}
		}
	}

	@Override
	public Set<QualifiedName> waitingForNamespaces() {
		synchronized (blocks) {
			return new HashSet<>(blocks.keySet());
		}
	}

	@Override
	public List<DataSource> waitingForIds(QualifiedName namespace) {
		synchronized (blocks) {
			return new ArrayList<>(blocks.get(namespace));
		}
	}

	@Override
	public void waitFor(QualifiedName namespace, DataSource id) throws InterruptedException {
		synchronized (blocks) {
			while (blocks.contains(namespace, id)) {
				blocks.wait();
			}
		}
	}

	@Override
	public void waitFor(QualifiedName namespace, DataSource id, long timeoutMilliseconds) throws InterruptedException {
		synchronized (blocks) {
			while (blocks.contains(namespace, id)) {
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
	public boolean isBlocked() {
		synchronized (blocks) {
			return !blocks.isEmpty();
		}
	}
}
