package uk.co.strangeskies.modabi.data.io.structured;

import java.util.function.Function;

import uk.co.strangeskies.modabi.data.io.TerminatingDataTarget;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public interface StructuredDataTarget {
	public StructuredDataTarget namespace(Namespace namespace);

	public StructuredDataTarget nextChild(QualifiedName name);

	public TerminatingDataTarget property(QualifiedName name);

	public default StructuredDataTarget property(QualifiedName name,
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
