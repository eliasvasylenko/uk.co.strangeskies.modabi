package uk.co.strangeskies.modabi.schema.processing.reference;

import uk.co.strangeskies.modabi.schema.model.Model;

public interface IncludeTarget {
	<T> void include(Model<T> model, T object);
}
