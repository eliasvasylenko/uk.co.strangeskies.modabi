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
package uk.co.strangeskies.modabi.schema.node;

import java.util.List;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.management.SchemaProcessingContext;
import uk.co.strangeskies.modabi.schema.management.ValueResolution;

public interface DataNode<T> extends
		BindingChildNode<T, DataNode<T>, DataNode.Effective<T>>,
		DataNodeChildNode<DataNode<T>, DataNode.Effective<T>> {
	interface Effective<T> extends DataNode<T>,
			BindingChildNode.Effective<T, DataNode<T>, Effective<T>>,
			DataNodeChildNode<DataNode<T>, Effective<T>> {
		@Override
		default void process(SchemaProcessingContext context) {
			context.accept(this);
		}

		@Override
		DataBindingType.Effective<T> type();

		List<T> providedValues();

		default T providedValue() {
			if (!Range.create(0, 1).contains(occurrences()))
				throw new SchemaException("Cannot request single value from node '"
						+ getName() + "' with occurrences '" + occurrences() + "'");

			if (providedValues() == null || providedValues().isEmpty())
				return null;
			else
				return providedValues().get(0);
		}
	}

	enum Format {
		PROPERTY, CONTENT, SIMPLE
	}

	Format format();

	default boolean isValueProvided() {
		return providedValueBuffer() != null;
	}

	DataSource providedValueBuffer();

	ValueResolution valueResolution();

	DataBindingType<T> type();

	Boolean optional();

	Boolean nullIfOmitted();
}
