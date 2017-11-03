package uk.co.strangeskies.modabi.schema.impl;

import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingConditionPrototype;
import uk.co.strangeskies.reflection.token.TypeToken;

public class BindingConditionFactory<T> {
  private final TypeToken<T> type;

  public BindingConditionFactory(TypeToken<T> type) {
    this.type = type;
  }

  public BindingCondition<T> create(BindingConditionPrototype builder) {
    return null; // TODO
  }
}
