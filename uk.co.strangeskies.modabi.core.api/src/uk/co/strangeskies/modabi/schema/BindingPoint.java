package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.reflection.token.TypeToken;

public interface BindingPoint<T> {
  TypeToken<T> dataType();

  /**
   * @return the model to bind to this point
   */
  Model<? super T> model();
}
