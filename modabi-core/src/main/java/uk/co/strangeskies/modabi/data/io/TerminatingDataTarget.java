package uk.co.strangeskies.modabi.data.io;

import java.util.function.Consumer;
import java.util.function.Function;

import uk.co.strangeskies.modabi.namespace.QualifiedName;

public interface TerminatingDataTarget extends DataTarget {
	public enum State {
		UNSTARTED, STARTED, TERMINATED
	}

	public State currentState();

	@Override
	public default <T> TerminatingDataTarget put(DataType<T> type, T data) {
		put(new DataItem<>(type, data));
		return this;
	}

	@Override
	public <T> TerminatingDataTarget put(DataItem<T> item);

	public void terminate();

	static TerminatingDataTarget composeString(Consumer<String> resultConsumer,
			Function<QualifiedName, String> qualifiedNameFormat) {
		return new TerminatingDataTargetDecorator(new TerminatingDataTarget() {
			private boolean terminated;
			private boolean compound;

			StringBuilder stringBuilder = new StringBuilder();

			private void next(Object value) {
				if (compound)
					stringBuilder.append(", ");
				else
					compound = true;
				stringBuilder.append(value);
			}

			@Override
			public <T> TerminatingDataTarget put(DataItem<T> item) {
				if (terminated)
					throw new IOException();

				if (item.type() == DataType.QUALIFIED_NAME) {
					next(qualifiedNameFormat.apply((QualifiedName) item.data()));
				} else
					next(item.data());
				return this;
			}

			@Override
			public void terminate() {
				resultConsumer.accept(stringBuilder.toString());

				terminated = true;
			}

			@Override
			public State currentState() {
				return null;
			}
		});
	}
}
