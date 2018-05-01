package uk.co.strangeskies.modabi.expression;

import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

class FloatLiteralExpression implements Expression {
  private final float value;

  FloatLiteralExpression(float value) {
    this.value = value;
  }

  @Override
  public Instructions compile(Scope scope) {
    return new Instructions(forClass(float.class), v -> v.floatLiteral(value));
  }

  @Override
  public String toString() {
    return value + "f";
  }
}
