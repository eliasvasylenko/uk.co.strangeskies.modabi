package uk.co.strangeskies.modabi.expression;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Objects;

public class NamedInvocationExpression implements Expression {
  private final String methodName;
  private final List<Expression> arguments;

  NamedInvocationExpression(String methodName, List<Expression> arguments) {
    this.methodName = methodName;
    this.arguments = arguments;
  }

  @Override
  public Instructions compile(Scope scope) {
    return scope
        .lookupInvocation(
            methodName,
            arguments.stream().map(e -> e.compile(scope)).collect(toList()));
  }

  @Override
  public String toString() {
    return methodName + arguments.stream().map(Objects::toString).collect(joining(", ", "(", ")"));
  }
}
