package uk.co.strangeskies.modabi.expression;

import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

class LongLiteralExpression implements Expression {
  private final long value;

  LongLiteralExpression(long value) {
    this.value = value;
  }

  @Override
  public Instructions compile(Scope scope) {
    return new Instructions(forClass(long.class), v -> v.longLiteral(value));
  }

  @Override
  public String toString() {
    return value + "l";
  }
}
