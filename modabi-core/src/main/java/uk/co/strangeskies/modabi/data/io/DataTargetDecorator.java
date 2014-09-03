package uk.co.strangeskies.modabi.data.io;

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
			throw new IOException("Cannot move to state '" + currentState
					+ "' from state '" + to + "'.");
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
