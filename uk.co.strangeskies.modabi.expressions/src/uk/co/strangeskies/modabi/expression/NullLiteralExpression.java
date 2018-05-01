package uk.co.strangeskies.modabi.expression;

import static uk.co.strangeskies.reflection.token.TypeToken.forNull;

public class NullLiteralExpression implements Expression {
  @Override
  public Instructions compile(Scope scope) {
    return new Instructions(forNull(), v -> v.nullLiteral());
  }

  @Override
  public String toString() {
    return "null";
  }
}
