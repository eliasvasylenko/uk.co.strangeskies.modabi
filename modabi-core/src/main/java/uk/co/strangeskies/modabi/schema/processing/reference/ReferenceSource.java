package uk.co.strangeskies.modabi.schema.processing.reference;

import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.node.model.Model;

public interface ReferenceSource {
	<T> T reference(Model<T> model, QualifiedName idDomain, DataSource id);
}
