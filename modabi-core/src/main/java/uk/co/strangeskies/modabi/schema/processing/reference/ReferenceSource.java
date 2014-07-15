package uk.co.strangeskies.modabi.schema.processing.reference;

import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.model.Model;

public interface ReferenceSource {
	<T> T reference(Model<T> model, String idDomain, BufferedDataSource id);
}
