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
import java.util.function.Function;

public class NamedSet<T> extends QualifiedNamedSet<T> {
	private Namespace namespace;
	private final Function<T, String> namingFunction;

	public NamedSet(final Namespace namespace,
			final Function<T, String> namingFunction) {
		super(t -> new QualifiedName(namingFunction.apply(t), namespace));
		this.namingFunction = namingFunction;
	}

	public boolean add(T element, Namespace namespace) {
		QualifiedName name = new QualifiedName(namingFunction.apply(element),
				namespace);
		if (getElements().get(name) != null)
			return false;

		getElements().put(name, element);
		return true;
	}

	public boolean addAll(Collection<? extends T> elements, Namespace namespace) {
		boolean changed = false;
		for (T element : elements) {
			changed = add(element, namespace) || changed;
		}
		return changed;
	}

	public T get(String name) {
		return get(new QualifiedName(name, namespace));
	}
}
