package uk.co.strangeskies.modabi.io;

public class DiscardingDataTarget extends DataTargetDecorator {
	public DiscardingDataTarget() {
		super(new DataTarget() {
			@Override
			public void terminate() {}

			@Override
			public <T> DataTarget put(DataItem<T> item) {
				return this;
			}

			@Override
			public DataStreamState currentState() {
				return null;
			}
		});
	}
}
