package uk.co.strangeskies.modabi.expression;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.modabi.expression.Expressions.argumentInstructionSequence;
import static uk.co.strangeskies.reflection.token.OverloadResolver.resolveOverload;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import java.util.List;
import java.util.Objects;

import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.TypeToken;

class ConstructorInvocationExpression implements Expression {
  private final Class<?> type;
  private final List<Expression> arguments;

  ConstructorInvocationExpression(Class<?> type, List<Expression> arguments) {
    this.type = type;
    this.arguments = arguments;
  }

  @Override
  public Instructions compile(Scope scope) {
    List<Instructions> argumentInstructions = arguments
        .stream()
        .map(a -> a.compile(scope))
        .collect(toList());

    List<TypeToken<?>> argumentTypes = argumentInstructions
        .stream()
        .map(m -> m.getResultType())
        .collect(toList());

    ExecutableToken<?, ?> executable = forClass(type)
        .infer()
        .constructors()
        .map(ExecutableToken::infer)
        .collect(resolveOverload(argumentTypes))
        .resolve();

    return new Instructions(
        executable.getReturnType(),
        v -> v
            .invokeConstructor(
                executable,
                argumentInstructionSequence(executable, argumentInstructions)));

  }

  @Override
  public String toString() {
    return "new " + type
        + arguments.stream().map(Objects::toString).collect(joining(", ", "(", ")"));
  }
}
