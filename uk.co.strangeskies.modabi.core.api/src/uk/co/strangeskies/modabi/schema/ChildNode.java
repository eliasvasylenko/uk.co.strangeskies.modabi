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
package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.reflection.TypeToken;

public interface ChildNode<S extends ChildNode<S>> extends SchemaNode<S> {
	TypeToken<?> preInputType();

	/**
	 * Default behavior is as if 1..1.
	 *
	 * @return
	 */
	Range<Integer> occurrences();

	/**
	 * Default behavior is as if true. If unordered, may input concurrently, and
	 * semantics of updating existing binding are more flexible. Also note that
	 * unordered nodes may bind and unbind with less memory-efficiency...
	 *
	 * @return
	 */
	Boolean ordered();

	TypeToken<?> postInputType();

	SchemaNode<?> parent();

	@Override
	ChildNodeConfigurator<?, S> configurator();
}
