package uk.co.strangeskies.modabi.data.io.structured;

import java.util.Arrays;

import uk.co.strangeskies.modabi.data.io.IOException;

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
					+ "' from state '" + this + "'.");
	}

	public void checkValid(StructuredDataState... validState) {
		if (!Arrays.asList(validState).contains(this))
			throw new IOException("Cannot perform action in state '" + this + "'.");
	}
}