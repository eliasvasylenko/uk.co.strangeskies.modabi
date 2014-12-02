package uk.co.strangeskies.modabi.types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import uk.co.strangeskies.modabi.types.Bound.BoundVisitor;

import com.google.common.reflect.TypeResolver;
import com.google.common.reflect.TypeToken;

public class BoundVerifier implements BoundVisitor<Boolean> {
	private final Map<InferenceVariable, Type> substitutions;

	public BoundVerifier() {
		substitutions = new HashMap<>();
	}

	private TypeResolver getResolver() {
		TypeResolver resolver = new TypeResolver();

		for (InferenceVariable var : substitutions.keySet())
			resolver = resolver.where(var.getTypeVariable(), substitutions.get(var));

		return resolver;
	}

	@Override
	public Boolean acceptEqual(InferenceVariable a, InferenceVariable b) {
		return TypeToken.of(substitutions.get(a)).equals(
				TypeToken.of(substitutions.get(b)));
	}

	@Override
	public Boolean acceptEqual(Type a, InferenceVariable b) {
		return TypeToken.of(getResolver().resolveType(a)).equals(
				TypeToken.of(substitutions.get(b)));
	}

	@Override
	public Boolean acceptSubtype(InferenceVariable a, InferenceVariable b) {
		return TypeToken.of(substitutions.get(b)).isAssignableFrom(
				TypeToken.of(substitutions.get(a)));
	}

	@Override
	public Boolean acceptSubtype(Type a, InferenceVariable b) {
		return TypeToken.of(substitutions.get(b)).isAssignableFrom(
				TypeToken.of(getResolver().resolveType(a)));
	}

	@Override
	public Boolean acceptSubtype(InferenceVariable a, Type b) {
		return TypeToken.of(getResolver().resolveType(b)).isAssignableFrom(
				TypeToken.of(substitutions.get(a)));
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
