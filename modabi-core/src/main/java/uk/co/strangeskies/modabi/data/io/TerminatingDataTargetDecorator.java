package uk.co.strangeskies.modabi.data.io;

import uk.co.strangeskies.utilities.Decorator;

public class TerminatingDataTargetDecorator extends
		Decorator<TerminatingDataTarget> implements TerminatingDataTarget {
	private State currentState;

	public TerminatingDataTargetDecorator(TerminatingDataTarget component) {
		super(component);

		currentState = State.UNSTARTED;
	}

	@Override
	public State currentState() {
		return currentState;
	}

	private void checkTransition(State to) {
		if (currentState == State.TERMINATED || to == State.UNSTARTED)
			throw new IOException("Cannot move to state '" + currentState
					+ "' from state '" + to + "'.");
		currentState = to;
	}

	@Override
	public <U> TerminatingDataTarget put(DataItem<U> item) {
		checkTransition(State.STARTED);
		getComponent().put(item);

		return this;
	}

	@Override
	public void terminate() {
		checkTransition(State.TERMINATED);
		getComponent().terminate();
	}
}
