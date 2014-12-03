package uk.co.strangeskies.modabi.types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import uk.co.strangeskies.modabi.types.Bound.BoundVisitor;

import com.google.common.reflect.TypeResolver;
import com.google.common.reflect.TypeToken;

public class Resolution {
	private final BoundSet bounds;
	private final Map<InferenceVariable, Type> instantiations;

	public Resolution(BoundSet bounds) {
		this.bounds = bounds;
		instantiations = new HashMap<>();
	}

	public boolean verify() {
		BoundVerifier verifier = new BoundVerifier();
		return bounds.stream().allMatch(b -> b.accept(verifier));
	}

	private class BoundVerifier implements BoundVisitor<Boolean> {
		private TypeResolver getResolver() {
			TypeResolver resolver = new TypeResolver();

			for (InferenceVariable var : bounds.getContext().getInferenceVariables())
				resolver = resolver.where(var.getTypeVariable(),
						instantiations.get(var));

			return resolver;
		}

		@Override
		public Boolean acceptEquality(InferenceVariable a, InferenceVariable b) {
			return TypeToken.of(instantiations.get(a)).equals(
					TypeToken.of(instantiations.get(b)));
		}

		@Override
		public Boolean acceptEquality(InferenceVariable a, Type b) {
			return TypeToken.of(instantiations.get(a)).equals(
					TypeToken.of(getResolver().resolveType(b)));
		}

		@Override
		public Boolean acceptSubtype(InferenceVariable a, InferenceVariable b) {
			return TypeToken.of(instantiations.get(b)).isAssignableFrom(
					TypeToken.of(instantiations.get(a)));
		}

		@Override
		public Boolean acceptSubtype(InferenceVariable a, Type b) {
			return TypeToken.of(getResolver().resolveType(b)).isAssignableFrom(
					TypeToken.of(instantiations.get(a)));
		}

		@Override
		public Boolean acceptSubtype(Type a, InferenceVariable b) {
			return TypeToken.of(instantiations.get(b)).isAssignableFrom(
					TypeToken.of(getResolver().resolveType(a)));
		}

		@Override
		public Boolean acceptFalsehood() {
			return false;
		}

		@Override
		public Boolean acceptCaptureConversion(Map<Type, InferenceVariable> c) {
			return true;
		}
	}
}
