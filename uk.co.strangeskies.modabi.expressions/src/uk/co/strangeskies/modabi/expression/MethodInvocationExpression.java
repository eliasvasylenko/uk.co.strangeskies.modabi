package uk.co.strangeskies.modabi.expression;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.modabi.expression.Expressions.argumentInstructionSequence;
import static uk.co.strangeskies.reflection.token.MethodMatcher.anyMethod;
import static uk.co.strangeskies.reflection.token.OverloadResolver.resolveOverload;

import java.util.List;
import java.util.Objects;

import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.TypeToken;

class MethodInvocationExpression implements Expression {
  private final Expression receiver;
  private final String methodName;
  private final List<Expression> arguments;

  MethodInvocationExpression(Expression receiver, String methodName, List<Expression> arguments) {
    this.receiver = receiver;
    this.methodName = methodName;
    this.arguments = arguments;
  }

  @Override
  public Instructions compile(Scope scope) {
    Instructions receiverInstructions = receiver.compile(scope);
    List<Instructions> argumentInstructions = arguments
        .stream()
        .map(a -> a.compile(scope))
        .collect(toList());

    List<TypeToken<?>> argumentTypes = argumentInstructions
        .stream()
        .map(m -> m.getResultType())
        .collect(toList());

    System.out.println(receiverInstructions.getResultType() + " .. " + methodName);

    ExecutableToken<?, ?> executable = receiverInstructions
        .getResultType()
        .methods()
        .filter(anyMethod().named(methodName))
        .map(ExecutableToken::infer)
        .collect(resolveOverload(argumentTypes))
        .resolve();

    return new Instructions(
        executable.getReturnType(),
        v -> v
            .invokeMember(
                receiverInstructions,
                executable,
                argumentInstructionSequence(executable, argumentInstructions)));
  }

  @Override
  public String toString() {
    return receiver + "." + methodName
        + arguments.stream().map(Objects::toString).collect(joining(", ", "(", ")"));
  }
}
