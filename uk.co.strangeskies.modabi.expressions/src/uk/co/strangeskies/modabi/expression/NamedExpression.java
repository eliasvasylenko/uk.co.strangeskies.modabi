package uk.co.strangeskies.modabi.expression;

public class NamedExpression implements MutableExpression {
  private final String variableName;

  NamedExpression(String variableName) {
    this.variableName = variableName;
  }

  @Override
  public Instructions compile(Scope scope) {
    return scope.lookupVariable(variableName);
  }

  @Override
  public Expression assign(Expression value) {
    return new NamedAssignmentExpression(variableName, value);
  }

  @Override
  public String toString() {
    return variableName;
  }
}
