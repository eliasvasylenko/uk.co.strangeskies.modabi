package uk.co.strangeskies.modabi.expression;

import static java.util.stream.Collectors.joining;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import uk.co.strangeskies.reflection.token.TypeToken;

public class ExpressionWriter implements ExpressionVisitor {
  private final Deque<String> stack = new ArrayDeque<>();

  public String evaluate(Expression expression) {
    expression.evaluate(this);
    return stack.pop();
  }

  private String evaluateArguments(List<Expression> arguments) {
    return "(" + arguments.stream().map(this::evaluate).collect(joining(", ")) + ")";
  }

  private void complete(String string) {
    stack.push(string);
  }

  @Override
  public <T> void visitCast(TypeToken<T> type, Expression expression) {
    complete("((" + type + ")" + evaluate(expression) + ")");
  }

  @Override
  public <T> void visitCheck(TypeToken<T> type, Expression expression) {
    complete("([" + type + "]" + evaluate(expression) + ")");
  }

  @Override
  public void visitField(Expression receiver, String variable) {
    complete(evaluate(receiver) + "." + variable);
  }

  @Override
  public void visitFieldAssignment(Expression receiver, String variable, Expression value) {
    complete(evaluate(receiver) + "." + variable + " = " + evaluate(value));
  }

  @Override
  public void visitStaticField(Class<?> type, String variable) {
    complete(type.getName() + "." + variable);
  }

  @Override
  public void visitStaticFieldAssignment(Class<?> type, String variable, Expression value) {
    complete(type.getName() + "." + variable + " = " + evaluate(value));
  }

  @Override
  public void visitInvocation(Expression receiver, String method, List<Expression> arguments) {
    complete(evaluate(receiver) + "." + method + evaluateArguments(arguments));
  }

  @Override
  public <T> void visitConstructorInvocation(Class<T> type, List<Expression> arguments) {
    complete("new " + type.getName() + evaluateArguments(arguments));
  }

  @Override
  public <T> void visitStaticInvocation(Class<T> type, String method, List<Expression> arguments) {
    complete(type.getName() + "." + method + evaluateArguments(arguments));
  }

  @Override
  public void visitNull() {
    complete("null");
  }

  @Override
  public void visitLiteral(Object value) {
    if (value instanceof String) {
      complete("\"" + value + "\"");
    } else if (value instanceof Number) {
      complete(value.toString()); // TODO
    } else if (value instanceof Class<?>) {
      complete(((Class<?>) value).getName() + ".class");
    }
  }

  @Override
  public void visitIteration(Expression value) {
    complete(evaluate(value) + "[]");
  }

  @Override
  public void visitNamed(String name) {
    complete(name);
  }

  @Override
  public void visitNamedAssignment(String name, Expression value) {
    complete(name + " = " + evaluate(value));
  }

  @Override
  public void visitNamedInvocation(String name, List<Expression> arguments) {
    complete(name + evaluateArguments(arguments));
  }
}
