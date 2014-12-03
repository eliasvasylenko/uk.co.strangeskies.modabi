package uk.co.strangeskies.modabi.types;

import java.lang.reflect.Type;
import java.util.Map;

public interface Bound {
	public interface BoundVisitor<T> {
		public T acceptEquality(InferenceVariable a, InferenceVariable b);

		public T acceptEquality(InferenceVariable a, Type b);

		public T acceptSubtype(InferenceVariable a, InferenceVariable b);

		public T acceptSubtype(InferenceVariable a, Type b);

		public T acceptSubtype(Type a, InferenceVariable b);

		public T acceptFalsehood();

		public T acceptCaptureConversion(Map<Type, InferenceVariable> c);
	}

	public class PartialBoundVisitor<T> implements BoundVisitor<T> {
		@Override
		public T acceptEquality(InferenceVariable a, InferenceVariable b) {
			return null;
		}

		@Override
		public T acceptEquality(InferenceVariable a, Type b) {
			return null;
		}

		@Override
		public T acceptSubtype(InferenceVariable a, InferenceVariable b) {
			return null;
		}

		@Override
		public T acceptSubtype(InferenceVariable a, Type b) {
			return null;
		}

		@Override
		public T acceptSubtype(Type a, InferenceVariable b) {
			return null;
		}

		@Override
		public T acceptFalsehood() {
			return null;
		}

		@Override
		public T acceptCaptureConversion(Map<Type, InferenceVariable> c) {
			return null;
		}

	}

	<T> T accept(BoundVisitor<T> visitor);
}
