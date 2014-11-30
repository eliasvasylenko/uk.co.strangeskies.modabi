package uk.co.strangeskies.modabi.schema.management.providers;

import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.node.model.Model;

public interface DereferenceSource {
	<T> T reference(Model<T> model, QualifiedName idDomain, DataSource id);
}
