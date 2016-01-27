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

import uk.co.strangeskies.utilities.Decorator;

public class DataTargetDecorator extends Decorator<DataTarget> implements
		DataTarget {
	private DataStreamState currentState;

	public DataTargetDecorator(DataTarget component) {
		super(component);

		currentState = DataStreamState.UNSTARTED;
	}

	@Override
	public DataStreamState currentState() {
		return currentState;
	}

	private void checkTransition(DataStreamState to) {
		if (currentState == DataStreamState.TERMINATED || to == DataStreamState.UNSTARTED)
			throw new ModabiIOException("Cannot move to state '" + currentState
					+ "' from state '" + to + "'");
		currentState = to;
	}

	@Override
	public <U> DataTarget put(DataItem<U> item) {
		checkTransition(DataStreamState.STARTED);
		getComponent().put(item);

		return this;
	}

	@Override
	public void terminate() {
		checkTransition(DataStreamState.TERMINATED);
		getComponent().terminate();
	}
}
