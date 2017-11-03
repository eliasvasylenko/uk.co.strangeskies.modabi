package uk.co.strangeskies.modabi.expression.impl;

import java.util.function.Function;

import uk.co.strangeskies.modabi.binding.impl.BindingContextImpl;
import uk.co.strangeskies.reflection.token.TypeToken;

public class CompiledExpression<T> {
  private final TypeToken<T> type;
  private final Function<BindingContextImpl, T> function;

  public CompiledExpression(TypeToken<T> type, Function<BindingContextImpl, T> function) {
    this.type = type;
    this.function = function;
  }

  public Function<BindingContextImpl, T> getFunction() {
    return function;
  }

  public TypeToken<?> getType() {
    return type;
  }
}
