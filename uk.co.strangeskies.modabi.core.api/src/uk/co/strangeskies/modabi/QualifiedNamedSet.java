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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import uk.co.strangeskies.utilities.collection.ObservableSet;
import uk.co.strangeskies.utilities.collection.SynchronizedObservableSet;
import uk.co.strangeskies.utilities.function.SetTransformationView;

public class QualifiedNamedSet<T> extends /* @ReadOnly */SynchronizedObservableSet<QualifiedNamedSet<T>, T> {
	private final Function<T, QualifiedName> qualifiedNamingFunction;
	private final LinkedHashMap<QualifiedName, T> elements;

	public QualifiedNamedSet(Function<T, QualifiedName> namingFunction) {
		this(namingFunction, new LinkedHashMap<>());
	}

	private QualifiedNamedSet(Function<T, QualifiedName> namingFunction, LinkedHashMap<QualifiedName, T> elements) {
		super(ObservableSet.over(new SetTransformationView<T, T>(elements.values(), Function.identity()) {
			@Override
			public boolean add(T e) {
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

		qualifiedNamingFunction = namingFunction;
		this.elements = elements;
	}

	protected Map<QualifiedName, T> getElements() {
		return elements;
	}

	public T get(QualifiedName name) {
		return elements.get(name);
	}

	@Override
	public String toString() {
		return elements.toString();
	}

	@Override
	public QualifiedNamedSet<T> copy() {
		QualifiedNamedSet<T> copy = new QualifiedNamedSet<>(qualifiedNamingFunction, new LinkedHashMap<>());
		copy.addAll(this);
		return copy;
	}
}
