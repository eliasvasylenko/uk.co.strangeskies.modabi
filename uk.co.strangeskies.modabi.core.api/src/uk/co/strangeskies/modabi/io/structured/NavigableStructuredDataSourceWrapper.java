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
package uk.co.strangeskies.modabi.io.structured;

public class NavigableStructuredDataSourceWrapper extends
		StructuredDataSourceWrapper implements NavigableStructuredDataSource {
	private final StructuredDataState initialState;

	public NavigableStructuredDataSourceWrapper(
			NavigableStructuredDataSource component) {
		super(component);

		initialState = currentState();
	}

	public NavigableStructuredDataSourceWrapper(
			NavigableStructuredDataSource component,
			StructuredDataState initialState) {
		super(component);

		this.initialState = initialState;
	}

	@Override
	protected NavigableStructuredDataSource getComponent() {
		return (NavigableStructuredDataSource) super.getComponent();
	}

	@Override
	public NavigableStructuredDataSource copy() {
		return new NavigableStructuredDataSourceWrapper(getComponent().copy());
	}

	@Override
	public void reset() {
		getComponent().reset();
		setState(initialState);
	}
}
