package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.reflection.token.TypeToken;

public interface BindingPoint<T> {
	/**
	 * The required data type for the binding. This type implies two basic
	 * constraints to the binding process:
	 * 
	 * - The {@link Binding#getDataType() exact type } of the resulting binding
	 * should be assignable to this type.
	 * 
	 * - In some cases this type may be more specific than the type of the
	 * {@link #model()}, but should at least be assignable to a type supported by
	 * the model.
	 * 
	 * @return the type of the binding point
	 */
	TypeToken<T> type();

	/**
	 * Get the model to bind to this point.
	 * 
	 * This may be the root model/null TODO there is currently no concept of a root
	 * model, need to decide on this. It may also be extensible, and if extensible
	 * it may also be abstract.
	 * 
	 * As this model may be extensible, this means it may not be the most specific
	 * model ultimately used for any given binding made to this point. Rather, it
	 * gives an upper bound on the model which may be used.
	 * 
	 * @return the model to bind to this point
	 */
	Model<? super T> model();
}
