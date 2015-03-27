/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.modabi.namespace;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.collection.decorator.SetDecorator;
import uk.co.strangeskies.utilities.function.collection.SetTransformationView;

public class QualifiedNamedSet<T> extends /* @ReadOnly */SetDecorator<T> {
	private final Function<T, QualifiedName> qualifiedNamingFunction;
	private final LinkedHashMap<QualifiedName, T> elements;

	public QualifiedNamedSet(Function<T, QualifiedName> namingFunction) {
		super(new IdentityProperty<Set<T>>());

		qualifiedNamingFunction = namingFunction;
		elements = new LinkedHashMap<>();

		getComponentProperty()
				.set(
						new SetTransformationView<T, T>(elements.values(), Function
								.identity()));
	}

	protected Map<QualifiedName, T> getElements() {
		return elements;
	}

	@Override
	public boolean add(T element) {
		QualifiedName name = qualifiedNamingFunction.apply(element);
		if (elements.get(name) != null)
			return false;

		elements.put(name, element);

		return true;
	}

	@Override
	public boolean addAll(Collection<? extends T> elements) {
		boolean changed = false;
		for (T element : elements) {
			changed = add(element) || changed;
		}
		return changed;
	}

	public T get(QualifiedName name) {
		return elements.get(name);
	}

	@Override
	public String toString() {
		return elements.toString();
	}
}
