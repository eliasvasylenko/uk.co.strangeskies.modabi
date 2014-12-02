package uk.co.strangeskies.modabi.types;

import java.util.HashSet;
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

	public void add(Bound bound) {
		bounds.add(bound);
	}
}
