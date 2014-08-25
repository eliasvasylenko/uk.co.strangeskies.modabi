package uk.co.strangeskies.modabi.schema.processing.impl;

import uk.co.strangeskies.modabi.model.Model;

public interface IncludeTarget {
	<T> void include(Model<T> model, T object);
}
