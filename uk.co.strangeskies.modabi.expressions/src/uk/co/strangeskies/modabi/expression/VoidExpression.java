package uk.co.strangeskies.modabi.expression;

public class VoidExpression implements Expression {
  @Override
  public Instructions compile(Scope scope) {
    throw new UnsupportedOperationException();
  }
}
