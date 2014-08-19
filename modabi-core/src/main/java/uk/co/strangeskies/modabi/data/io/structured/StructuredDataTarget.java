package uk.co.strangeskies.modabi.data.io.structured;

import java.util.function.Consumer;
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
	public StructuredDataTarget defaultNamespaceHint(Namespace namespace);

	/**
	 * This method should return a locally sound representation of the qualified
	 * name consistent with the target's concept of namespace qualification. A
	 * basic XML implementation, for example, might generate a random
	 * document-unique id: '&lt;generated-uid&gt;'. It might then insert an
	 * 'xmlns' attribute mapping this generated id as an alias to a html
	 * representation of the Namespace object associated with the QualifiedName
	 * object, then return a matching string of
	 * '&lt;generated-uid&gt;:&lt;name&gt;'.
	 *
	 * For human readable formats the returned value should be consistent with the
	 * way the qualified name is represented for a 'child', where possible. This
	 * would be the case in the XML example given above.
	 *
	 * @param name
	 * @return
	 */
	public default String composeQualifiedName(QualifiedName name) {
		return name.toString();
	}

	public default StructuredDataTarget composeQualifiedName(QualifiedName name,
			Consumer<String> compositionTarget) {
		compositionTarget.accept(composeQualifiedName(name));
		return this;
	}

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
