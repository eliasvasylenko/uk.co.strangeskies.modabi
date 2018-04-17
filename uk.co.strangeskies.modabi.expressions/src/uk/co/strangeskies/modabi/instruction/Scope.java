package uk.co.strangeskies.modabi.instruction;

import java.util.List;

public interface Scope {
  Instructions lookupVariable(String variableName);

  Instructions lookupVariableAssignment(String variableName, Instructions value);

  Instructions lookupInvocation(String invocationName, List<Instructions> arguments);
}
