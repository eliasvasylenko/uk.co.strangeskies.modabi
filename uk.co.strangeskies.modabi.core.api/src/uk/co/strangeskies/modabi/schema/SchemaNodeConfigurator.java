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

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.reflection.codegen.ValueExpression;

/**
 * 
 * @author Elias N Vasylenko
 *
 * @param <T,
 *          E> the data type of the node, as defined by the binding point it
 *          attaches to
 * @param <E>
 *          the end product of the configurator, which will either be the parent
 *          schema node configurator, or in the case of the root, a factory for
 *          the model
 */
public interface SchemaNodeConfigurator<T, E> {
	SchemaNodeConfigurator<T, E> concrete(boolean concrete);

	Optional<Boolean> getConcrete();

	SchemaNodeConfigurator<T, E> extensible(boolean extensible);

	Optional<Boolean> getExtensible();

	default SchemaNodeConfigurator<T, E> baseModel(Model<? super T> baseModel) {
		return baseModel(asList(baseModel));
	}

	SchemaNodeConfigurator<T, E> baseModel(Collection<? extends Model<? super T>> baseModel);

	Stream<Model<?>> getBaseModel();

	InputInitializerConfigurator initializeInput();

	OutputInitializerConfigurator<?> initializeOutput();

	default SchemaNodeConfigurator<T, E> initializeInput(
			Function<? super InputInitializerConfigurator, ? extends ValueExpression<?>> initializer) {
		initializeInput().expression(initializer.apply(initializeInput()));
		return this;
	}

	default SchemaNodeConfigurator<T, E> initializeOutput(
			Function<? super OutputInitializerConfigurator<?>, ? extends ValueExpression<?>> initializer) {
		initializeOutput().expression(initializer.apply(initializeOutput()));
		return this;
	}

	ChildBindingPointConfigurator<?, SchemaNodeConfigurator<T, E>> addChildBindingPoint();

	default SchemaNodeConfigurator<T, E> addChildBindingPoint(
			Function<ChildBindingPointConfigurator<?, SchemaNodeConfigurator<T, E>>, SchemaNodeConfigurator<T, E>> configuration) {
		configuration.apply(addChildBindingPoint()).endNode();

		return this;
	}

	List<ChildBindingPointConfigurator<?, SchemaNodeConfigurator<T, E>>> getChildBindingPoints();

	SchemaNodeConfigurator<T, E> valueResolution(ValueResolution registrationTime);

	Optional<ValueResolution> getValueResolution();

	SchemaNodeConfigurator<T, E> provideValue(DataSource buffer);

	Optional<DataSource> getProvidedValue();

	E endNode();
}
