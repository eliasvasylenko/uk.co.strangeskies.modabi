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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Function;

import uk.co.strangeskies.utilities.collection.ObservableSet;
import uk.co.strangeskies.utilities.collection.SynchronizedObservableSet;
import uk.co.strangeskies.utilities.function.SetTransformationView;

public abstract class QualifiedNamedSet<S extends QualifiedNamedSet<S, T>, T>
		extends /* @ReadOnly */SynchronizedObservableSet<S, T> implements Scoped<S> {
	private final S parent;
	private final LinkedHashMap<QualifiedName, T> elements;

	public QualifiedNamedSet(Function<T, QualifiedName> namingFunction) {
		this(namingFunction, null);
	}

	protected QualifiedNamedSet(Function<T, QualifiedName> namingFunction, S parent) {
		this(namingFunction, parent, new LinkedHashMap<>());
	}

	private QualifiedNamedSet(Function<T, QualifiedName> namingFunction, S parent,
			LinkedHashMap<QualifiedName, T> elements) {
		super(ObservableSet.over(new SetTransformationView<T, T>(elements.values(), identity()) {
			@Override
			public boolean add(T e) {
				if (parent != null && parent.contains(e))
					return false;

				QualifiedName name = namingFunction.apply(e);
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
		}));

		this.parent = parent;
		this.elements = elements;

		if (parent != null) {
			Set<T> silent = silent();

			parent.changes().addObserver(change -> {
				/*
				 * If we add items to the parent which are currently in the child, we
				 * must silently remove them, and modify the change event so that those
				 * additions are not seen from the child scope when we forward it...
				 */
				Set<T> effectivelyAdded = null;
				for (T item : change.added()) {
					if (silent.remove(item)) {
						if (effectivelyAdded == null) {
							effectivelyAdded = new HashSet<>(change.added());
						}
						effectivelyAdded.remove(item);
					}
				}

				Change<T> effectiveChange;
				if (effectivelyAdded == null) {
					effectiveChange = change;
				} else {
					if (effectivelyAdded.isEmpty() && change.removed().isEmpty()) {
						/*
						 * No items were *effectively* added, and none were removed, so we
						 * can drop the event.
						 */
						return;
					} else {
						effectiveChange = wrapChange(change, effectivelyAdded);
					}
				}

				/*
				 * Forward change events
				 */
				parent.changes().fire(effectiveChange);
				parent.fire(getThis());
			});
		}
	}

	private Change<T> wrapChange(Change<T> change, Set<T> effectivelyAdded) {
		return new Change<T>() {
			@Override
			public Set<T> added() {
				return effectivelyAdded;
			}

			@Override
			public Set<T> removed() {
				return change.removed();
			}
		};
	}

	public T get(QualifiedName name) {
		T element = elements.get(name);
		if (element == null && parent != null) {
			element = parent.get(name);
		}
		return element;
	}

	@Override
	public synchronized Iterator<T> iterator() {
		Iterator<T> iterator = QualifiedNamedSet.super.iterator();
		Iterator<T> parentIterator = parent != null ? parent.iterator() : Collections.<T> emptySet().iterator();

		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext() || parentIterator.hasNext();
			}

			@Override
			public T next() {
				return iterator.hasNext() ? iterator.next() : parentIterator.next();
			}
		};
	}

	@Override
	public boolean isEmpty() {
		return super.isEmpty() && (parent == null || parent.isEmpty());
	}

	@Override
	public String toString() {
		return elements.toString();
	}

	@Override
	public S getParentScope() {
		return parent;
	}

	@Override
	public synchronized void collapseIntoParentScope() {
		parent.silent().addAll(this);
		silent().clear();
	}
}
