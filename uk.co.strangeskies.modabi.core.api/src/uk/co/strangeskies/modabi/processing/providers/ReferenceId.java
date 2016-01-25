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
package uk.co.strangeskies.modabi.processing.providers;

import uk.co.strangeskies.reflection.TypeToken;

public class ReferenceId<T> {
	private final TypeToken<T> type;
	private final T object;
	private final String id;

	public ReferenceId(TypeToken<T> type, T object, String id) {
		this.type = type;
		this.object = object;
		this.id = id;
	}

	public TypeToken<T> getType() {
		return type;
	}

	public T getObject() {
		return object;
	}

	public String getId() {
		return id;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof ReferenceId
				&& object.equals(((ReferenceId<?>) other).getObject());
	}

	@Override
	public int hashCode() {
		return object.hashCode();
	}
}
