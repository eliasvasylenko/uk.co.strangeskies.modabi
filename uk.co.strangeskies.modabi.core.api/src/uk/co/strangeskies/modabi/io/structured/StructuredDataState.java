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
package uk.co.strangeskies.modabi.io.structured;

import java.util.Arrays;

import uk.co.strangeskies.modabi.io.IOException;

public enum StructuredDataState {
	UNSTARTED, ELEMENT_START, POPULATED_ELEMENT, ELEMENT_WITH_CONTENT, PROPERTY, CONTENT, FINISHED;

	public StructuredDataState enterState(StructuredDataState next) {
		switch (this) {
		case UNSTARTED:
			checkExitStateValid(next, ELEMENT_START);
			break;
		case ELEMENT_START:
			checkExitStateValid(next, ELEMENT_START, POPULATED_ELEMENT, PROPERTY,
					CONTENT, FINISHED);
			break;
		case POPULATED_ELEMENT:
			checkExitStateValid(next, ELEMENT_START, POPULATED_ELEMENT, FINISHED);
			break;
		case ELEMENT_WITH_CONTENT:
			checkExitStateValid(next, POPULATED_ELEMENT, FINISHED);
			break;
		case PROPERTY:
			checkExitStateValid(next, ELEMENT_START);
			break;
		case CONTENT:
			checkExitStateValid(next, ELEMENT_WITH_CONTENT);
			break;
		case FINISHED:
			checkExitStateValid(next);
			break;
		}

		return next;
	}

	private void checkExitStateValid(StructuredDataState exitState,
			StructuredDataState... validExitState) {
		if (!Arrays.asList(validExitState).contains(exitState))
			throw new IOException("Cannot move to state '" + exitState
					+ "' from state '" + this + "'");
	}

	public void checkValid(StructuredDataState... validState) {
		if (!Arrays.asList(validState).contains(this))
			throw new IOException("Cannot perform action in state '" + this + "'");
	}
}
