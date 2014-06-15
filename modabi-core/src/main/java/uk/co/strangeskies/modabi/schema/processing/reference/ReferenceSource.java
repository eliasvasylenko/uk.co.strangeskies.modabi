package uk.co.strangeskies.modabi.schema.processing.reference;

import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public interface ReferenceSource {
	<T> T reference(Model<T> model, String idDomain, QualifiedName id);
}
