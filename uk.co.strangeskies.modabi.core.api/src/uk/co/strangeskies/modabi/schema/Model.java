package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.Schema;

public interface Model<T> extends BindingPoint<T> {
	Schema schema();
}
