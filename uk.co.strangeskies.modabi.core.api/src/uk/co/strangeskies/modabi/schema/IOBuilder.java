package uk.co.strangeskies.modabi.schema;

import static uk.co.strangeskies.modabi.schema.expression.Expressions.typeToken;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.expression.ValueExpression;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface IOBuilder {
  ValueExpression none();

  /*
   * TODO Scoping here deals only with sibling binding points. Dealing with others
   * requires bringing them into scope explicitly.
   * 
   * (Do we bring them into scope through some standard mechanism? Or just by way
   * of ProcessingContext#getBindingObject etc...)
   */
  default ValueExpression provideFor(ChildBindingPoint<?> type) {
    return provide(type.dataType());
  }

  default ValueExpression provide(TypeToken<?> type) {
    return context().invoke("provide", typeToken(type)).invoke("getObject");
  }

  default ValueExpression provide(Class<?> type) {
    return provide(TypeToken.forClass(type));
  }

  /**
   * Get a value expression evaluating to a provision as per
   * {@link #provide(TypeToken)}. The type provided is as declared as the
   * {@link BindingPoint#dataType() data type} of the nearest containing binding
   * point.
   * 
   * @return
   */
  ValueExpression provide();

  ValueExpression context();

  ValueExpression binding(String bindingPoint);

  default ValueExpression bound(String bindingPoint) {
    return binding(bindingPoint).invoke("getData");
  }

  ValueExpression binding(QualifiedName bindingPoint);

  default ValueExpression bound(QualifiedName bindingPoint) {
    return binding(bindingPoint).invoke("getData");
  }

  ValueExpression binding(ChildBindingPoint<?> bindingPoint);

  default ValueExpression bound(ChildBindingPoint<?> bindingPoint) {
    return binding(bindingPoint).invoke("getData");
  }
}
