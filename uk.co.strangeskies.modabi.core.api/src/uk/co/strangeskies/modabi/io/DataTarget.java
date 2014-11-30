package uk.co.strangeskies.modabi.io;

import java.util.function.Consumer;
import java.util.function.Function;

import uk.co.strangeskies.modabi.namespace.QualifiedName;

public interface DataTarget {
	public default <T> DataTarget put(DataType<T> type, T data) {
		return put(DataItem.forDataOfType(type, data));
	}

	public <T> DataTarget put(DataItem<T> item);

	public DataStreamState currentState();

	public void terminate();

	static DataTarget composeString(Consumer<String> resultConsumer,
			Function<QualifiedName, String> qualifiedNameFormat) {
		return new DataTargetDecorator(new DataTarget() {
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
			public <T> DataTarget put(DataItem<T> item) {
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
			public DataStreamState currentState() {
				return null;
			}
		});
	}
}
