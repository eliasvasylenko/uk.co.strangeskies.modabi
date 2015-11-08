package uk.co.strangeskies.modabi.processing.providers;

import uk.co.strangeskies.reflection.TypeToken;

public class ReferenceId<T> {
	private final TypeToken<T> type;
	private final T object;
	private final String id;

	public ReferenceId(TypeToken<T> type, T object, String id) {
		this.type = type;
		this.object = object;
		this.id = id;
	}

	public TypeToken<T> getType() {
		return type;
	}

	public T getObject() {
		return object;
	}

	public String getId() {
		return id;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof ReferenceId
				&& object.equals(((ReferenceId<?>) other).getObject());
	}

	@Override
	public int hashCode() {
		return object.hashCode();
	}
}
