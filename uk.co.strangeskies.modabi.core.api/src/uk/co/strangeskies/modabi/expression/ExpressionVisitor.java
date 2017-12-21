package uk.co.strangeskies.modabi.expression;

import java.util.List;

import uk.co.strangeskies.reflection.token.TypeToken;

public interface ExpressionVisitor {
  <T> void visitCast(TypeToken<T> type, Expression expression);

  void visitField(Expression receiver, String variable);

  void visitFieldAssignment(Expression receiver, String variable, Expression value);

  void visitInvocation(Expression receiver, String method, List<Expression> arguments);

  <T> void visitConstructorInvocation(Class<T> type, List<Expression> arguments);

  <T> void visitStaticInvocation(Class<T> type, String method, List<Expression> arguments);

  void visitNull();

  void visitLiteral(Object value);

  void visitIteration(Expression value);

  void visitNamed(String name);

  void visitNamedAssignment(String name, Expression value);

  void visitNamedInvocation(String name, List<Expression> arguments);
}
