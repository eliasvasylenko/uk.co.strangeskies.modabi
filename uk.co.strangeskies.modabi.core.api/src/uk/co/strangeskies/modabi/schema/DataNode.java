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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.NodeProcessor;
import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypedObject;

public interface DataNode<T> extends BindingChildNode<T, DataNode<T>> {
	@Override
	default void process(NodeProcessor context) {
		context.accept(this);
	}

	@Override
	default List<DataType<? super T>> base() {
		List<DataType<? super T>> base = new ArrayList<>();

		DataType<? super T> baseComponent = type();
		do {
			base.add(baseComponent);
			baseComponent = baseComponent.baseType();
		} while (baseComponent != null);

		return base;
	}

	List<T> providedValues();

	default List<TypedObject<T>> typedProvidedValues() {
		return providedValues().stream().map(dataType()::typedObject).collect(Collectors.toList());
	}

	default T providedValue() {
		if (!Range.between(0, 1).contains(occurrences()))
			throw new ModabiException(t -> t.cannotProvideSingleValue(name(), occurrences()));

		if (providedValues() == null || providedValues().isEmpty())
			return null;
		else
			return providedValues().get(0);
	}

	default TypedObject<T> typedProvidedValue() {
		return dataType().typedObject(providedValue());
	}

	enum Format {
		/**
		 * 
		 */
		PROPERTY,

		/**
		 * 
		 */
		CONTENT,

		/**
		 * 
		 */
		SIMPLE
	}

	Format format();

	default boolean isValueProvided() {
		return providedValueBuffer() != null;
	}

	DataSource providedValueBuffer();

	ValueResolution valueResolution();

	DataType<? super T> type();

	@Override
	default TypeToken<DataNode<T>> getThisType() {
		return new TypeToken<DataNode<T>>() {}.withTypeArgument(new TypeParameter<T>() {}, dataType());
	}

	@Override
	DataNodeConfigurator<T> configurator();
}
