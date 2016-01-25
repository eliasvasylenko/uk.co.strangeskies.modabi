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

import java.time.LocalDate;

public class QualifiedName {
	private final String name;
	private final Namespace namespace;

	public QualifiedName(String name, Namespace namespace) {
		this.name = name;
		this.namespace = namespace;
	}

	public QualifiedName(Class<?> name, LocalDate date) {
		this.name = name.getSimpleName();
		this.namespace = new Namespace(name.getPackage(), date);
	}

	public QualifiedName(String name) {
		this(name, Namespace.getDefault());
	}

	public String getName() {
		return name;
	}

	public Namespace getNamespace() {
		return namespace;
	}

	@Override
	public String toString() {
		return namespace + ":" + name;
	}

	public String toHttpString() {
		return namespace.toHttpString() + name;
	}

	public static QualifiedName parseString(String string) {
		int splitIndex = string.lastIndexOf(':');

		return new QualifiedName(string.substring(splitIndex + 1),
				Namespace.parseString(string.substring(0, splitIndex)));
	}

	public static QualifiedName parseHttpString(String string) {
		int splitIndex = string.lastIndexOf('/');

		return new QualifiedName(string.substring(splitIndex + 1),
				Namespace.parseHttpString(string.substring(0, splitIndex + 1)));
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof QualifiedName))
			return false;

		return namespace.equals(((QualifiedName) obj).namespace)
				&& name.equals(((QualifiedName) obj).name);
	}

	@Override
	public int hashCode() {
		return name.hashCode() ^ namespace.hashCode();
	}
}
