package uk.co.strangeskies.modabi.schema.processing.reference;

import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public interface DereferenceTarget {
	<T> QualifiedName dereference(Model<T> model, String idDomain, T object);
}
