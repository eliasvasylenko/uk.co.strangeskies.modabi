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
package uk.co.strangeskies.modabi;

import static java.util.function.Function.identity;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Scoped;
import uk.co.strangeskies.utilities.collection.ObservableSet;
import uk.co.strangeskies.utilities.collection.ScopedObservableSet;
import uk.co.strangeskies.utilities.collection.SynchronizedObservableSet;
import uk.co.strangeskies.utilities.function.SetTransformationView;

public abstract class NamedSet<S extends NamedSet<S, N, T>, N, T> extends /* @ReadOnly */ScopedObservableSet<S, T>
		implements Scoped<S> {
	private final Function<T, N> namingFunction;
	private final LinkedHashMap<N, T> elements;
	private final Object mutex;

	public NamedSet(Function<T, N> namingFunction) {
		this(namingFunction, null);
	}

	protected NamedSet(Function<T, N> namingFunction, S parent) {
		this(namingFunction, parent, new LinkedHashMap<>());
	}

	private NamedSet(Function<T, N> namingFunction, S parent, LinkedHashMap<N, T> elements) {
		this(parent, namingFunction, elements, SynchronizedObservableSet
				.over(ObservableSet.over(new SetTransformationView<T, T>(elements.values(), identity()) {
					@Override
					public boolean add(T e) {
						N name = namingFunction.apply(e);
						if (elements.get(name) != null)
							return false;

						return elements.putIfAbsent(name, e) == null;
					}

					@Override
					public boolean addAll(Collection<? extends T> elements) {
						boolean changed = false;
						for (T element : elements) {
							changed = add(element) || changed;
						}
						return changed;
					}
				})));
	}

	private NamedSet(S parent, Function<T, N> namingFunction, LinkedHashMap<N, T> elements,
			SynchronizedObservableSet<?, T> set) {
		super(parent, set);

		this.namingFunction = namingFunction;
		this.elements = elements;
		this.mutex = set.getMutex();
	}

	public N nameOf(T element) {
		return namingFunction.apply(element);
	}

	protected Object getMutex() {
		return mutex;
	}

	public T get(N name) {
		synchronized (getMutex()) {
			T element = elements.get(name);
			if (element == null) {
				element = getParentScope().map(p -> p.get(name)).orElse(null);
			}
			return element;
		}
	}

	public T waitForGet(N name) throws InterruptedException {
		return waitForGet(name, () -> {});
	}

	public T waitForGet(N name, Runnable onPresent) throws InterruptedException {
		return waitForGet(name, onPresent, -1);
	}

	public T waitForGet(N name, int timeoutMilliseconds) throws InterruptedException {
		return waitForGet(name, () -> {}, timeoutMilliseconds);
	}

	public T waitForGet(N name, Runnable onPresent, int timeoutMilliseconds) throws InterruptedException {
		IdentityProperty<T> result = new IdentityProperty<>();

		synchronized (getMutex()) {
			Function<Change<T>, Boolean> observer = c -> {
				synchronized (getMutex()) {
					if (result.get() != null) {
						return true;
					}

					for (T element : c.added()) {
						if (nameOf(element).equals(name)) {
							onPresent.run();

							result.set(element);
							getMutex().notifyAll();

							return true;
						}
					}

					return false;
				}
			};

			changes().addTerminatingObserver(observer);

			T element = get(name);

			if (element != null) {
				onPresent.run();

				result.set(element);
				changes().removeTerminatingObserver(observer);
			} else {
				try {
					do {
						if (timeoutMilliseconds < 0) {
							getMutex().wait();
						} else {
							getMutex().wait(timeoutMilliseconds);
						}
					} while (result.get() == null);
				} catch (InterruptedException e) {
					if (result.get() == null) {
						changes().removeTerminatingObserver(observer);
						throw e;
					}
				}
			}
		}

		return result.get();
	}

	public Map<N, T> getElements() {
		Map<N, T> elements = new HashMap<>(this.elements);
		getParentScope().ifPresent(p -> elements.putAll(p.getElements()));
		return elements;
	}

	@Override
	public String toString() {
		return getElements().toString();
	}
}
