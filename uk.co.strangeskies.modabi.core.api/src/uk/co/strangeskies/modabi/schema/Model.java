package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.reflection.TypeToken;

public interface Model<T> extends BindingPoint<T> {
	@Override
	TypeToken<Model<T>> getThisType();
}
