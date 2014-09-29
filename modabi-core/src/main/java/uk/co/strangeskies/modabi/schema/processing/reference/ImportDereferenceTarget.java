package uk.co.strangeskies.modabi.schema.processing.reference;

import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.node.model.Model;

public interface ImportDereferenceTarget {
	<T> DataSource dereferenceImport(Model<T> model, QualifiedName idDomain,
			T object);
}
