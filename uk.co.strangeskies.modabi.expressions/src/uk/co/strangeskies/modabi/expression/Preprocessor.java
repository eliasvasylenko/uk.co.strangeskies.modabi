package uk.co.strangeskies.modabi.expression;

import static java.util.stream.Collectors.toList;

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

  default Expression process(Expression expression) {
    return v -> {
      if (v == visitor())
        expression.evaluate(this);
      else
        expression.evaluate(v);
    };
  }

  default List<Expression> processList(List<Expression> arguments) {
    return arguments.stream().map(this::process).collect(toList());
  }

  @Override
  default <T> void visitCast(TypeToken<T> type, Expression expression) {
    visitor().visitCast(type, process(expression));
  }

  @Override
  default <T> void visitCheck(TypeToken<T> type, Expression expression) {
    visitor().visitCheck(type, process(expression));
  }

  @Override
  default void visitField(Expression receiver, String variable) {
    visitor().visitField(process(receiver), variable);
  }

  @Override
  default void visitFieldAssignment(Expression receiver, String variable, Expression value) {
    visitor().visitFieldAssignment(process(receiver), variable, v -> value.evaluate(this));
  }

  @Override
  default void visitStaticField(Class<?> type, String variable) {
    visitor().visitStaticField(type, variable);
  }

  @Override
  default void visitStaticFieldAssignment(Class<?> type, String variable, Expression value) {
    visitor().visitStaticFieldAssignment(type, variable, value);
  }

  @Override
  default void visitInvocation(Expression receiver, String method, List<Expression> arguments) {
    visitor().visitInvocation(process(receiver), method, processList(arguments));
  }

  @Override
  default <T> void visitConstructorInvocation(Class<T> type, List<Expression> arguments) {
    visitor().visitConstructorInvocation(type, processList(arguments));
  }

  @Override
  default <T> void visitStaticInvocation(Class<T> type, String method, List<Expression> arguments) {
    visitor().visitStaticInvocation(type, method, processList(arguments));
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
    visitor().visitIteration(process(value));
  }

  @Override
  default void visitNamed(String name) {
    visitor().visitNamed(name);
  }

  @Override
  default void visitNamedAssignment(String name, Expression value) {
    visitor().visitNamedAssignment(name, process(value));
  }

  @Override
  default void visitNamedInvocation(String name, List<Expression> arguments) {
    visitor().visitNamedInvocation(name, processList(arguments));
  }
}
