package uk.co.strangeskies.modabi.expression;

import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.modabi.expression.ExpressionException.MESSAGES;

import java.util.List;

public interface Scope {
  default Instructions lookupVariable(String variableName) {
    throw new ExpressionException(MESSAGES.cannotResolveVariable(variableName));
  }

  default Instructions lookupVariableAssignment(String variableName, Instructions value) {
    throw new ExpressionException(
        MESSAGES.cannotResolveVariable(variableName, value.getResultType()));
  }

  default Instructions lookupInvocation(String invocationName, List<Instructions> arguments) {
    throw new ExpressionException(
        MESSAGES
            .cannotResolveInvocation(
                invocationName,
                arguments.stream().map(Instructions::getResultType).collect(toList())));
  }
}
