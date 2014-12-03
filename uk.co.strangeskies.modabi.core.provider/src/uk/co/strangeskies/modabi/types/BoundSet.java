package uk.co.strangeskies.modabi.types;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BoundSet {
	private final InferenceContext context;

	private final Set<Bound> bounds;

	public BoundSet(InferenceContext context) {
		this.context = context;
		bounds = new HashSet<>();
	}

	public InferenceContext getContext() {
		return context;
	}

	void addEquality(InferenceVariable a, InferenceVariable b) {
		bounds.add(v -> v.acceptEqual(a, b));
	}

	void addEquality(Type a, InferenceVariable b) {
		bounds.add(v -> v.acceptEqual(a, b));
	}

	void addEquality(InferenceVariable a, Type b) {
		bounds.add(v -> v.acceptEqual(a, b));
	}

	void addSubtype(InferenceVariable a, InferenceVariable b) {
		bounds.add(v -> v.acceptSubtype(a, b));
	}

	void addSubtype(Type a, InferenceVariable b) {
		bounds.add(v -> v.acceptSubtype(a, b));
	}

	void addSubtype(InferenceVariable a, Type b) {
		bounds.add(v -> v.acceptSubtype(a, b));
	}

	void addFalsehood() {
		bounds.add(v -> v.acceptFalsehood());
	}

	void addCaptureConversion(Map<Type, InferenceVariable> c) {
		bounds.add(v -> v.acceptCaptureConversion(c));
	}
}
