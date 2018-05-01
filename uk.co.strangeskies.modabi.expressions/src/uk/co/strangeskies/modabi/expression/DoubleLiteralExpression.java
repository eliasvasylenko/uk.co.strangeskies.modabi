package uk.co.strangeskies.modabi.expression;

import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

class DoubleLiteralExpression implements Expression {
  private final double value;

  DoubleLiteralExpression(double value) {
    this.value = value;
  }

  @Override
  public Instructions compile(Scope scope) {
    return new Instructions(forClass(double.class), v -> v.doubleLiteral(value));
  }

  @Override
  public String toString() {
    return value + "d";
  }
}
