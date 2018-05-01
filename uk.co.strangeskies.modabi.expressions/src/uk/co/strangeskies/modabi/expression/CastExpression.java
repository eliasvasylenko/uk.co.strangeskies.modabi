package uk.co.strangeskies.modabi.expression;

import uk.co.strangeskies.reflection.token.TypeToken;

public class CastExpression implements Expression {
  private final Expression expression;
  private final TypeToken<?> type;

  public CastExpression(Expression expression, TypeToken<?> type) {
    this.expression = expression;
    this.type = type;
  }

  public Expression getExpression() {
    return expression;
  }

  public TypeToken<?> getType() {
    return type;
  }

  @Override
  public Instructions compile(Scope scope) {
    Instructions valueInstructions = expression.compile(scope);

    /*
     * TODO cast check is not yet supported, must imply necessary bounds
     */
    // if (!type.isCastableFrom(valueMetadata.type))
    // throw new ExpressionException(MESSAGES.cannotPerformCast(type,
    // valueMetadata.type));

    return new Instructions(type, v -> v.cast(type, valueInstructions));
  }

  @Override
  public String toString() {
    return "(" + type + ")" + expression;
  }
}
