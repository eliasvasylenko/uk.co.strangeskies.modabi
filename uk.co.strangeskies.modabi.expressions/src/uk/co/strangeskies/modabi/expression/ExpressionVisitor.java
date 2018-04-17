package uk.co.strangeskies.modabi.expression;

import java.util.List;

import uk.co.strangeskies.reflection.token.TypeToken;

public interface ExpressionVisitor {
  <T> void visitCast(TypeToken<T> type, Expression expression);

  /**
   * This is similar to the behavior of a cast with some subtle differences.
   * 
   * Rather than the expression making the cast taking the given type, it retains
   * the type of the given expression.
   * 
   * Rather than the given expression being checked for cast-compatibility with
   * the given type, it creates a loose-compatibility bound with the given type.
   * 
   * @param type
   * @param expression
   */
  <T> void visitCheck(TypeToken<T> type, Expression expression);

  void visitField(Expression receiver, String variable);

  void visitStaticField(Class<?> type, String variable);

  void visitFieldAssignment(Expression receiver, String variable, Expression value);

  void visitStaticFieldAssignment(Class<?> type, String variable, Expression value);

  void visitInvocation(Expression receiver, String method, List<Expression> arguments);

  <T> void visitConstructorInvocation(Class<T> type, List<Expression> arguments);

  <T> void visitStaticInvocation(Class<T> type, String method, List<Expression> arguments);

  void visitNull();

  void visitLiteral(Object value);

  // TODO document how this fits in with the type inference/evaluation model etc.
  void visitIteration(Expression value);

  void visitNamed(String name);

  void visitNamedAssignment(String name, Expression value);

  void visitNamedInvocation(String name, List<Expression> arguments);
}
