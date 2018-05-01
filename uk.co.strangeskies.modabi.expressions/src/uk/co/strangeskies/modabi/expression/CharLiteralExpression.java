package uk.co.strangeskies.modabi.expression;

import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

class CharLiteralExpression implements Expression {
  private final char value;

  CharLiteralExpression(char value) {
    this.value = value;
  }

  @Override
  public Instructions compile(Scope scope) {
    return new Instructions(forClass(char.class), v -> v.intLiteral(value));
  }

  @Override
  public String toString() {
    return "'" + value + "'";
  }
}
