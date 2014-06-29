package uk.co.strangeskies.modabi.schema.processing.reference;

import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.modabi.namespace.QualifiedName;

public interface RelativeReferenceSource {
	default Object reference(int parentLevel, QualifiedName... elementId) {
		return reference(parentLevel, Arrays.asList(elementId));
	}

	Object reference(int parentLevel, List<QualifiedName> elementId);
}
