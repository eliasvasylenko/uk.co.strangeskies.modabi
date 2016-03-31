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
import java.util.LinkedHashMap;
import java.util.function.Function;

import uk.co.strangeskies.utilities.Scoped;
import uk.co.strangeskies.utilities.collection.ObservableSet;
import uk.co.strangeskies.utilities.collection.ScopedObservableSet;
import uk.co.strangeskies.utilities.function.SetTransformationView;

public abstract class QualifiedNamedSet<S extends QualifiedNamedSet<S, T>, T>
		extends /* @ReadOnly */ScopedObservableSet<S, T> implements Scoped<S> {
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
		}).synchronizedView());

		this.elements = elements;
	}

	public T get(QualifiedName name) {
		T element = elements.get(name);
		if (element == null) {
			element = getParentScope().map(p -> p.get(name)).orElse(null);
		}
		return element;
	}

	@Override
	public String toString() {
		return elements.toString();
	}
}
