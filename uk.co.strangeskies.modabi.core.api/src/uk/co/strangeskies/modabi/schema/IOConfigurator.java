package uk.co.strangeskies.modabi.schema;

import static uk.co.strangeskies.modabi.schema.expression.Expressions.typeTokenExpression;
import static uk.co.strangeskies.reflection.token.MethodMatcher.anyMethod;

import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.schema.expression.ValueExpression;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface IOConfigurator {
  <U> ValueExpression<U> none();

  /*
   * TODO Scoping here deals only with sibling binding points. Dealing with others
   * requires bringing them into scope explicitly.
   * 
   * (Do we bring them into scope through some standard mechanism? Or just by way
   * of ProcessingContext#getBindingObject etc...)
   */
  <U> ValueExpression<U> provideFor(ChildBindingPoint<U> type);

  default <U> ValueExpression<U> provide(TypeToken<U> type) {
    return context().invoke(anyMethod().named("provide"), typeTokenExpression(type)).invoke(
        anyMethod().named("getObject").returning(type));
  }

  default <U> ValueExpression<U> provide(Class<U> type) {
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
  ValueExpression<?> provide();

  ValueExpression<ProcessingContext> context();

  ValueExpression<?> bound(String string);
}
