package uk.co.strangeskies.modabi.expression;

public class NamedAssignmentExpression implements Expression {
  private final String variableName;
  private final Expression value;

  public NamedAssignmentExpression(String variableName, Expression value) {
    this.variableName = variableName;
    this.value = value;
  }

  @Override
  public Instructions compile(Scope scope) {
    return scope.lookupVariableAssignment(variableName, value.compile(scope));
  }

  @Override
  public String toString() {
    return "(" + variableName + " = " + value + ")";
  }
}
