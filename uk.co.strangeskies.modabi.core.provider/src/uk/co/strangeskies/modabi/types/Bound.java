package uk.co.strangeskies.modabi.types;

import java.lang.reflect.Type;
import java.util.Map;

public interface Bound {
	public interface BoundVisitor<T> {
		public T acceptEqual(InferenceVariable a, InferenceVariable b);

		public T acceptEqual(Type a, InferenceVariable b);

		public T acceptSubtype(InferenceVariable a, InferenceVariable b);

		public T acceptSubtype(Type a, InferenceVariable b);

		public T acceptSubtype(InferenceVariable a, Type b);

		public T acceptFalsehood();

		public T acceptCaptureConversion(Map<Type, InferenceVariable> c);
	}

	void accept(BoundVisitor<?> visitor);

	static Bound equality(InferenceVariable a, InferenceVariable b) {
		return v -> v.acceptEqual(a, b);
	}

	static Bound equality(Type a, InferenceVariable b) {
		return v -> v.acceptEqual(a, b);
	}

	static Bound subtype(InferenceVariable a, InferenceVariable b) {
		return v -> v.acceptSubtype(a, b);
	}

	static Bound subtype(Type a, InferenceVariable b) {
		return v -> v.acceptSubtype(a, b);
	}

	static Bound subtype(InferenceVariable a, Type b) {
		return v -> v.acceptSubtype(a, b);
	}

	static Bound falsehood() {
		return v -> v.acceptFalsehood();
	}

	static Bound captureConversion(Map<Type, InferenceVariable> c) {
		return v -> v.acceptCaptureConversion(c);
	}
}
