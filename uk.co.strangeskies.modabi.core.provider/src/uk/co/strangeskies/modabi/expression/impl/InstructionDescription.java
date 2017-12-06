package uk.co.strangeskies.modabi.expression.impl;

import uk.co.strangeskies.reflection.token.TypeToken;

public class InstructionDescription {
  public final TypeToken<?> type;

  public InstructionDescription(TypeToken<?> type) {
    this.type = type;
  }
}
