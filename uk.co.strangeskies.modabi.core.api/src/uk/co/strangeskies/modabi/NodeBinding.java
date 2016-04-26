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

import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.reflection.TypedObject;

public abstract class NodeBinding<T, N extends BindingNode.Effective<T, ?, ?>> {
	private final N node;
	private final T data;

	public NodeBinding(N node, T data) {
		this.node = node;
		this.data = data;
	}

	public N getNode() {
		return node;
	}

	public T getData() {
		return data;
	}

	public TypedObject<T> getTypedData() {
		return TypedObject.castInto(node.dataType(), data);
	}

	@Override
	public String toString() {
		return data + " : " + node;
	}

	// public void updateData();

	// public StructuredDataSource getSource();

	// public void updateSource();
}
