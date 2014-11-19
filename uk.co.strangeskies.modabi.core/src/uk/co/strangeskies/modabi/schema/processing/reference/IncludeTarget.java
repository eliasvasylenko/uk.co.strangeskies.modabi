package uk.co.strangeskies.modabi.schema.processing.reference;

import java.util.Collection;

import uk.co.strangeskies.modabi.schema.node.model.Model;

public interface IncludeTarget {
	<T> void include(Model<T> model, T object);

	default <T> void include(Model<T> model, Collection<? extends T> objects) {
		for (T object : objects)
			include(model, object);
	}
}
