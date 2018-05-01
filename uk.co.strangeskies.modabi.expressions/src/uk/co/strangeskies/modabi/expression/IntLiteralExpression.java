package uk.co.strangeskies.modabi.expression;

import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

class IntLiteralExpression implements Expression {
  private final int value;

  IntLiteralExpression(int value) {
    this.value = value;
  }

  @Override
  public Instructions compile(Scope scope) {
    return new Instructions(forClass(int.class), v -> v.intLiteral(value));
  }

  @Override
  public String toString() {
    return Integer.toString(value);
  }
}
