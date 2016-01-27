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

import java.util.Arrays;

import uk.co.strangeskies.modabi.io.ModabiIOException;

public enum StructuredDataState {
	UNSTARTED, ELEMENT_START, POPULATED_ELEMENT, ELEMENT_WITH_CONTENT, PROPERTY, CONTENT, FINISHED;

	public StructuredDataState enterState(StructuredDataState next) {
		switch (this) {
		case UNSTARTED:
			assertExitStateValid(next, ELEMENT_START);
			break;
		case ELEMENT_START:
			assertExitStateValid(next, ELEMENT_START, POPULATED_ELEMENT, PROPERTY,
					CONTENT, FINISHED);
			break;
		case POPULATED_ELEMENT:
			assertExitStateValid(next, ELEMENT_START, POPULATED_ELEMENT, FINISHED);
			break;
		case ELEMENT_WITH_CONTENT:
			assertExitStateValid(next, POPULATED_ELEMENT, FINISHED);
			break;
		case PROPERTY:
			assertExitStateValid(next, ELEMENT_START);
			break;
		case CONTENT:
			assertExitStateValid(next, ELEMENT_WITH_CONTENT);
			break;
		case FINISHED:
			assertExitStateValid(next);
			break;
		}

		return next;
	}

	private void assertExitStateValid(StructuredDataState exitState,
			StructuredDataState... validExitState) {
		if (!Arrays.asList(validExitState).contains(exitState))
			throw new ModabiIOException(
					"Cannot move to state '" + exitState + "' from state '" + this + "'");
	}

	public void assertValid(StructuredDataState... validState) {
		if (!checkValid(validState))
			throw new ModabiIOException("Cannot perform action in state '" + this + "'");
	}

	public boolean checkValid(StructuredDataState... validState) {
		return Arrays.asList(validState).contains(this);
	}
}
