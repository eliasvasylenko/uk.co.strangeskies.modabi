package uk.co.strangeskies.modabi.expression;

import static java.util.Objects.requireNonNull;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

class StringLiteralExpression implements Expression {
  private final String value;

  StringLiteralExpression(String value) {
    this.value = requireNonNull(value);
  }

  @Override
  public Instructions compile(Scope scope) {
    return new Instructions(forClass(String.class), v -> v.stringLiteral(value));
  }

  @Override
  public String toString() {
    return '"' + value + '"';
  }
}
