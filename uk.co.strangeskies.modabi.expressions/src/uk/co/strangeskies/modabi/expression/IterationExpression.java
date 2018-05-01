package uk.co.strangeskies.modabi.expression;

import uk.co.strangeskies.reflection.token.TypeToken;

public class IterationExpression implements Expression {
  private final Expression iterable;

  public IterationExpression(Expression iterable) {
    this.iterable = iterable;
  }

  @Override
  public Instructions compile(Scope scope) {
    Instructions iterableInstructions = iterable.compile(scope);

    // TODO special cases for arrays and streams
    TypeToken<?> itemType = iterableInstructions
        .getResultType()
        .resolveSupertype(Iterable.class)
        .getTypeArguments()
        .findAny()
        .get()
        .getTypeToken();

    return new Instructions(itemType, v -> v.iterate(iterableInstructions));
  }

}
