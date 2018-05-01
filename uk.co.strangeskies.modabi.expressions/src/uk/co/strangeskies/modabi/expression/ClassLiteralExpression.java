package uk.co.strangeskies.modabi.expression;

import static java.util.Objects.requireNonNull;

import uk.co.strangeskies.reflection.token.TypeArgument;
import uk.co.strangeskies.reflection.token.TypeToken;

class ClassLiteralExpression implements Expression {
  private final Class<?> value;

  ClassLiteralExpression(Class<?> value) {
    this.value = requireNonNull(value);
  }

  @Override
  public Instructions compile(Scope scope) {
    return new Instructions(classType(value), v -> v.classLiteral(value));
  }

  private <T> TypeToken<Class<T>> classType(Class<T> value) {
    return new TypeToken<Class<T>>() {}.withTypeArguments(new TypeArgument<T>(value) {});
  }

  @Override
  public String toString() {
    return value.getName() + ".class";
  }
}
