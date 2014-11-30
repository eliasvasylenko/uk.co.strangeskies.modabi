package uk.co.strangeskies.modabi.io;

import uk.co.strangeskies.modabi.schema.SchemaException;
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
