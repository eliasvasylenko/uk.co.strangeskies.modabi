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
package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.reflection.TypeToken;

public interface BindingChildNodeConfigurator<S extends BindingChildNodeConfigurator<S, N, T>, N extends BindingChildNode<T, ?, ?>, T>
		extends BindingNodeConfigurator<S, N, T>, InputNodeConfigurator<S, N>,
		ChildNodeConfigurator<S, N> {
	S outMethod(String methodName);

	S outMethodUnchecked(boolean unchecked);

	S outMethodIterable(boolean iterable);

	S outMethodCast(boolean cast);

	S occurrences(Range<Integer> occuranceRange);

	S extensible(boolean extensible);

	S ordered(boolean ordered);

	/*
	 * TODO 'isOrdered' hint, for ranges above ..2, to help magically minimise
	 * impact of updating a 'ModifiableStructuredDataTarget' (e.g. saving over an
	 * existing XML document) by not considering it a violation of model equality
	 * to reorder from outMethod iterator.
	 */

	TypeToken<T> getExpectedTypeBounds();
}
