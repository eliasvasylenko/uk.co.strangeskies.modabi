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
package uk.co.strangeskies.modabi.io;

import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.utilities.Decorator;

public class DataSourceDecorator extends Decorator<DataSource> implements
		DataSource {
	private DataStreamState currentState;

	public DataSourceDecorator(DataSource component) {
		super(component);

		if (component == null)
			throw new SchemaException("FLIPPER");

		currentState = DataStreamState.UNSTARTED;
	}

	@Override
	public DataStreamState currentState() {
		return currentState;
	}

	private void transition(DataStreamState to) {
		currentState = to;
	}

	@Override
	public int index() {
		return getComponent().index();
	}

	@Override
	public DataItem<?> get() {
		return getComponent().get();
	}

	@Override
	public DataItem<?> peek() {
		return getComponent().peek();
	}

	@Override
	public <T extends DataTarget> T pipe(T target, int items) {
		return getComponent().pipe(target, items);
	}

	@Override
	public int size() {
		return getComponent().size();
	}

	@Override
	public DataSource reset() {
		transition(DataStreamState.UNSTARTED);
		return getComponent().reset();
	}

	@Override
	public DataSource copy() {
		return new DataSourceDecorator(getComponent().copy());
	}
}
