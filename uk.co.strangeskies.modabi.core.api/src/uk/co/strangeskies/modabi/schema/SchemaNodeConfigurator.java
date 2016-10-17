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

import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.reflection.codegen.ValueExpression;
import uk.co.strangeskies.utilities.Factory;
import uk.co.strangeskies.utilities.Self;

public interface SchemaNodeConfigurator extends Factory<SchemaNode>, Self<SchemaNodeConfigurator> {
	InputInitializerConfigurator initializeInput();

	OutputInitializerConfigurator<?> initializeOutput();

	default SchemaNodeConfigurator initializeInput(
			Function<? super InputInitializerConfigurator, ? extends ValueExpression<?>> initializer) {
		initializeInput().expression(initializer.apply(initializeInput()));
		return this;
	}

	default SchemaNodeConfigurator initializeOutput(
			Function<? super OutputInitializerConfigurator<?>, ? extends ValueExpression<?>> initializer) {
		initializeOutput().expression(initializer.apply(initializeOutput()));
		return this;
	}

	ChildBindingPointConfigurator<?> addChildBindingPoint();

	default SchemaNodeConfigurator addChildBindingPoint(
			Function<ChildBindingPointConfigurator<?>, ChildBindingPointConfigurator<?>> configuration) {
		configuration.apply(addChildBindingPoint()).create();

		return this;
	}

	List<ChildBindingPointConfigurator<?>> getChildBindingPoints();
}
