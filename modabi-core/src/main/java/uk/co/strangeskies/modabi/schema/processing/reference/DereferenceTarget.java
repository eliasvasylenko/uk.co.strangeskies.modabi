package uk.co.strangeskies.modabi.schema.processing.reference;

import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.model.Model;

public interface DereferenceTarget {
	<T> BufferedDataSource dereference(Model<T> model, String idDomain, T object);

	void dereference(Object object);
}
