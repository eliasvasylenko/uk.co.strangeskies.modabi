package uk.co.strangeskies.modabi.schema.processing.reference;

import uk.co.strangeskies.modabi.data.io.DataSource;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public interface ImportSource {
	<T> T importObject(Model<T> model, QualifiedName idDomain,
			DataSource id);
}
