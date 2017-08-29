package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface BindingPoint<T> {
  /**
   * The required data type for the binding. This type implies two basic
   * constraints to the binding process:
   * 
   * - The {@link Binding#getDataType() exact type } of the resulting binding
   * should be assignable to this type.
   * 
   * - This type should be *contained* by the {@link #model() model} of the
   * binding point. TODO is containment really necessary or will assignability be
   * better?
   * 
   * @return the type of the binding point
   */
  TypeToken<T> dataType();

  /**
   * @return the model to bind to this point
   */
  Model<? super T> model();

  static <T> BindingPoint<T> anonymous(TypeToken<T> dataType, Model<? super T> model) {
    return new BindingPoint<T>() {
      @Override
      public TypeToken<T> dataType() {
        return dataType;
      }

      @Override
      public Model<? super T> model() {
        return model;
      }
    };
  }
}
