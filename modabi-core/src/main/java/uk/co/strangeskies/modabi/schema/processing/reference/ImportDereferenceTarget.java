package uk.co.strangeskies.modabi.schema.processing.reference;

import uk.co.strangeskies.modabi.data.io.DataSource;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public interface ImportDereferenceTarget {
	<T> DataSource dereferenceImport(Model<T> model,
			QualifiedName idDomain, T object);
}
