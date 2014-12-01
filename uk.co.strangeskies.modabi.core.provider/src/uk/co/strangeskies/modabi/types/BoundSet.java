package uk.co.strangeskies.modabi.types;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class BoundSet {
	private final Set<Bound> bounds;

	public BoundSet() {
		bounds = new HashSet<>();
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
