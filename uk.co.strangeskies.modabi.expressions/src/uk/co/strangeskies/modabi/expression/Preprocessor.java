package uk.co.strangeskies.modabi.expression;

import java.util.List;

import uk.co.strangeskies.reflection.token.TypeToken;

/**
 * This interface can be implemented to define preprocessor behavior for
 * expression compilation, acting as a simple macro preprocessor.
 * <p>
 * All unqualified variables and function invocations are matched against
 * {@link #process(String)} and {@link #process(String, List)} respectively,
 * which may optionally expand them to complex expressions. The preprocessor may
 * then be applied recursively to the results of these expansions depending on
 * the implementation.
 * 
 * @author Elias N Vasylenko
 */
public interface Preprocessor extends ExpressionVisitor {
  ExpressionVisitor visitor();

  @Override
  default <T> void visitCast(TypeToken<T> type, Expression expression) {
    visitor().visitCast(type, expression);
  }

  @Override
  default void visitField(Expression receiver, String variable) {
    visitor().visitField(receiver, variable);
  }

  @Override
  default void visitFieldAssignment(Expression receiver, String variable, Expression value) {
    visitor().visitFieldAssignment(receiver, variable, value);
  }

  @Override
  default void visitInvocation(Expression receiver, String method, List<Expression> arguments) {
    visitor().visitInvocation(receiver, method, arguments);
  }

  @Override
  default <T> void visitConstructorInvocation(Class<T> type, List<Expression> arguments) {
    visitor().visitConstructorInvocation(type, arguments);
  }

  @Override
  default <T> void visitStaticInvocation(Class<T> type, String method, List<Expression> arguments) {
    visitor().visitStaticInvocation(type, method, arguments);
  }

  @Override
  default void visitNull() {
    visitor().visitNull();
  }

  @Override
  default void visitLiteral(Object value) {
    visitor().visitLiteral(value);
  }

  @Override
  default void visitIteration(Expression value) {
    visitor().visitIteration(value);
  }

  @Override
  default void visitNamed(String name) {
    visitor().visitNamed(name);
  }

  @Override
  default void visitNamedAssignment(String name, Expression value) {
    visitor().visitNamedAssignment(name, value);
  }

  @Override
  default void visitNamedInvocation(String name, List<Expression> arguments) {
    visitor().visitNamedInvocation(name, arguments);
  }
}
