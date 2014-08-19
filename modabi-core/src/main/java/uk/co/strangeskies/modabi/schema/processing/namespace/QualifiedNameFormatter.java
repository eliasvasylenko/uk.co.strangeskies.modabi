package uk.co.strangeskies.modabi.schema.processing.namespace;

import uk.co.strangeskies.modabi.namespace.QualifiedName;

public interface QualifiedNameFormatter {
	String compose(QualifiedName qualifiedName);
}
