package uk.co.strangeskies.modabi.schema.expression;

import java.util.List;

import uk.co.strangeskies.reflection.token.TypeToken;

public interface ExpressionVisitor {
  void visitReceiver();

  void visitCast(TypeToken<?> type, ValueExpression value);

  <T, U> void visitGetField(ValueExpression receiver, String variable);

  <T, U> void visitSetField(ValueExpression receiver, String variable, ValueExpression value);

  <T> void visitInvocation(
      ValueExpression receiver,
      String method,
      List<ValueExpression> arguments);

  <T> void visitConstructorInvocation(Class<?> type, List<ValueExpression> arguments);

  <T> void visitStaticInvocation(Class<?> type, String method, List<ValueExpression> arguments);

  void visitNull();

  <T> void visitLiteral(T value);

  void visitIteration(ValueExpression value);

  void visitGetParameter(int i);

  void visitSetParameter(int i, ValueExpression value);
}
