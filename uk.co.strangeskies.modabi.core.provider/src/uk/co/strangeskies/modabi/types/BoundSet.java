package uk.co.strangeskies.modabi.types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class BoundSet {
	private final Set<Bound> bounds;

	private final Map<InferenceVariable, Type> instantiations;

	public BoundSet() {
		bounds = new HashSet<>();

		instantiations = new HashMap<>();
	}

	public BoundSet(Bound bound) {
		this();

		add(bound);
	}

	public BoundSet(boolean truth) {
		this();

		if (!truth)
			add(Bound.falsehood());
	}

	public Stream<Bound> stream() {
		return bounds.stream();
	}

	public void add(Bound bound) {
		bounds.add(bound);
	}
}
