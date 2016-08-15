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

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.TypeToken;

public interface ChildNodeConfigurator<S extends ChildNodeConfigurator<S, N>, N extends ChildNode<N>>
		extends SchemaNodeConfigurator<S, N> {
	/**
	 * Here we can just provide a string name instead of a fully qualified name,
	 * and the namespace of the parent node will be used.
	 *
	 * @param name
	 * @return
	 */
	S name(String name);

	S occurrences(Range<Integer> occuranceRange);

	Range<Integer> getOccurrences();

	default S optional(boolean optional) {
		return optional ? occurrences(Range.between(0, 1)) : occurrences(Range.between(1, 1));
	}

	Boolean getOptional();

	S orderedOccurrences(boolean ordered);

	Boolean getOrderedOccurrences();

	S postInputType(String postInputType);

	S postInputType(TypeToken<?> postInputType);

	default S postInputType(AnnotatedType unbindingType) {
		return postInputType(TypeToken.over(unbindingType));
	}

	default S postInputType(Type bindingType) {
		return postInputType(TypeToken.over(AnnotatedTypes.over(bindingType)));
	}

	TypeToken<?> getPostInputType();

	String getPostInputTypeString();
}
