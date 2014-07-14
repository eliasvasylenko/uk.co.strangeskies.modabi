package uk.co.strangeskies.modabi.schema.processing.reference;

import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public interface RelativeReferenceSource {
	default <T> T reference(Model<T> model, int parentLevel,
			QualifiedName... elementId) {
		return reference(model, parentLevel, Arrays.asList(elementId));
	}

	<T> T reference(Model<T> model, int parentLevel, List<QualifiedName> elementId);
}
