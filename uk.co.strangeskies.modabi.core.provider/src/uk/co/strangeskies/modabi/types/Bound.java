package uk.co.strangeskies.modabi.types;

import java.lang.reflect.Type;
import java.util.Map;

public interface Bound {
	interface BoundVisitor {
		void acceptEquality(InferenceVariable a, InferenceVariable b);

		void acceptEquality(InferenceVariable a, Type b);

		void acceptSubtype(InferenceVariable a, InferenceVariable b);

		void acceptSubtype(InferenceVariable a, Type b);

		void acceptSubtype(Type a, InferenceVariable b);

		void acceptFalsehood();

		void acceptCaptureConversion(Map<Type, InferenceVariable> c);
	}

	class PartialBoundVisitor implements BoundVisitor {
		@Override
		public void acceptEquality(InferenceVariable a, InferenceVariable b) {}

		@Override
		public void acceptEquality(InferenceVariable a, Type b) {}

		@Override
		public void acceptSubtype(InferenceVariable a, InferenceVariable b) {}

		@Override
		public void acceptSubtype(InferenceVariable a, Type b) {}

		@Override
		public void acceptSubtype(Type a, InferenceVariable b) {}

		@Override
		public void acceptFalsehood() {}

		@Override
		public void acceptCaptureConversion(Map<Type, InferenceVariable> c) {}
	}

	void accept(BoundVisitor visitor);
}
