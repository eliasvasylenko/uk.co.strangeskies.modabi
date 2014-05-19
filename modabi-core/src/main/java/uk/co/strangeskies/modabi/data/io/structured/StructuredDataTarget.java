package uk.co.strangeskies.modabi.data.io.structured;

import java.util.function.Function;

import uk.co.strangeskies.modabi.data.io.TerminatingDataTarget;

public interface StructuredDataTarget {
	public StructuredDataTarget nextChild(String name);

	public TerminatingDataTarget property(String name);

	public default StructuredDataTarget property(String name,
			Function<TerminatingDataTarget, TerminatingDataTarget> targetOperation) {
		TerminatingDataTarget target = property(name);
		if (target != targetOperation.apply(target))
			throw new IllegalArgumentException();
		target.terminate();
		return this;
	}

	public TerminatingDataTarget content();

	public default StructuredDataTarget content(
			Function<TerminatingDataTarget, TerminatingDataTarget> targetOperation) {
		TerminatingDataTarget target = content();
		if (target != targetOperation.apply(target))
			throw new IllegalArgumentException();
		target.terminate();
		return this;
	}

	public StructuredDataTarget endChild();
}
