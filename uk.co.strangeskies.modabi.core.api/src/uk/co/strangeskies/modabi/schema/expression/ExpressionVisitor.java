package uk.co.strangeskies.modabi.schema.expression;

import java.util.List;

import uk.co.strangeskies.reflection.token.MethodMatcher;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.VariableMatcher;

public interface ExpressionVisitor {
  void visitCast(TypeToken<?> type, ValueExpression<?> value);

  <T, U> void visitGetField(ValueExpression<? extends T> receiver, VariableMatcher<T, U> variable);

  <T, U> void visitSetField(
      ValueExpression<? extends T> receiver,
      VariableMatcher<T, U> variable,
      ValueExpression<? extends U> value);

  <T> void visitInvocation(
      ValueExpression<? extends T> receiver,
      MethodMatcher<T, ?> method,
      List<ValueExpression<?>> arguments);

  <T> void visitStaticInvocation(
      Class<?> type,
      MethodMatcher<?, ?> method,
      List<ValueExpression<?>> arguments);

  void visitNull();

  <T> void visitLiteral(T value);
}
