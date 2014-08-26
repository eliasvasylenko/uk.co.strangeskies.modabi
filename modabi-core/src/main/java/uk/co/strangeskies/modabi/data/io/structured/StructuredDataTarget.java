package uk.co.strangeskies.modabi.data.io.structured;

import java.util.function.Function;

import uk.co.strangeskies.modabi.data.io.TerminatingDataTarget;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public interface StructuredDataTarget {
	/**
	 * This may help some data targets, e.g. XML, organise content a little more
	 * cleanly, by suggesting a default namespace for a document at the current
	 * element (or the root element if none have been yet created). It may be
	 * ignored by data targets for which there is no useful analogue.
	 *
	 * @param namespace
	 * @return
	 */
	public StructuredDataTarget registerDefaultNamespaceHint(Namespace namespace);

	public StructuredDataTarget registerNamespaceHint(Namespace namespace);

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
